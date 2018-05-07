/**
 * Copyright (c) 2018, Foothill-De Anza Community College District
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.fhda.uportal.confgraph

import edu.fhda.uportal.confgraph.loader.GraphStore
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.PreDestroy
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Loads one or more YAML config files from a location on disk into an internal multi-valued
 * dictionary, keyed by the uPortal PAGS group that each config file is assigned to. When a
 * portlet user request is received, the config service can be queried to find which config
 * trees apply to that specific user group. All of the appliable config trees are deep-merged into
 * a final tree that can be returned back to the users' browser.
 *
 * @author mrapczynski , Foothill-De Anza College District, rapczynskimatthew@fhda.edu
 * @version 1.0
 */
class ConfigGraphService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigGraphService.class)

    private final SortedSet<GraphDefinition> graphs = new ConcurrentSkipListSet<>()

    private final GraphStore graphStore

    private ScheduledExecutorService scheduler

    /**
     * Create a new instance of ConfigGraphService. YAML files are loaded through deep iteration
     * in the location you provide.
     * @param basePath Path on disk to iterate through
     */
    ConfigGraphService(GraphStore store) {
        this.graphStore = store

        // Perform the initial load of the graph files
        this.load()
    }

    @PreDestroy
    void destroy() {
        // If not null, shutdown the auto-refresh scheduler
        scheduler?.shutdown()
    }

    void load() {
        logger.debug("Starting load process of configuration graph")

        // Lookup latest graph objects from S3
        graphStore.load { GraphDefinition graph ->
            // Remove old graph entry, if it exists
            graphs.remove(graph)

            // Add new graph entry
            graphs.add(graph)
        }

        logger.debug("Completed load process of configuration graph objectCount=${graphs.size()}")
    }

    void reload() {
        // Delegate to load(...)
        this.load()
    }

    void enableAutoRefresh() {
        def parent = this

        // Create new scheduled thread pool
        scheduler = Executors.newScheduledThreadPool(1)
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            void run() {
                logger.info("Running scheduled auto-refresh")
                parent.reload()
            }
        }, 120l, 60l, TimeUnit.SECONDS)
        logger.info("Enabled auto-refresh of config graph")
    }

    /**
     * Iterate the internal library of config graphs loaded from configured graph store, identify configurations
     * that a user is entitled to via group membership, and combine each config into a final output tree.
     * @param securityValidator Groovy closure for filtering config graphs.
     * @return A complete user configuration object
     */
    Map buildGraph(Closure<Boolean> securityValidator) {
        return this.buildGraph(null, null, securityValidator)
    }

    /**
     * Iterate the internal library of config graphs loaded from configured graph store, identify configurations
     * that a user is entitled to via group membership, and combine each config into a final output tree.
     * @param filter Jexl expression to refine which graphs can be returned for each matching definition
     * @param securityValidator Groovy closure for filtering config graphs.
     * @return A complete user configuration object
     */
    Map buildGraph(String filter, Closure<Boolean> securityValidator) {
        return this.buildGraph(filter, null, securityValidator)
    }

    /**
     * Iterate the internal library of config graphs loaded from configured graph store, identify configurations
     * that a user is entitled to via group membership, and combine each config into a final output tree.
     * @param filter Jexl expression to refine which graphs can be returned for each matching definition
     * @param subpath Jexl expression to execute against the graph after filtering, i.e to extract only a portion of the tree
     * @param securityValidator Groovy closure for filtering config graphs.
     * @return A complete user configuration object
     */
    Map buildGraph(String filter, String subpath, Closure<Boolean> securityValidator) {
        // Create empty result object
        Map root = new HashMap()

        // Iterate over each graph definition
        graphs
            .each { GraphDefinition graphDefinition ->
                // Evaluate security expressions, and look for a match
                def result = graphDefinition.securityExpressions.any { expression -> securityValidator.call(expression, graphDefinition) }

                // If we found a match, then apply the graph to the final output
                if(result) {
                    // Is a filter provided?
                    if(filter != null) {
                        // Execute the filter expression against the graph
                        if(graphDefinition.matches(filter)) {
                            // Merge the graph into the final result
                            root = MapUtilities.deepMerge(root, graphDefinition.get(subpath))
                        }
                    }
                    else {
                        // Merge the graph into the final result
                        root = MapUtilities.deepMerge(root, graphDefinition.get(subpath))
                    }
                }
        }

        // Return final object
        return root
    }

    /**
     * Iterate the internal library of config graphs loaded from configured graph store, identify configurations
     * that a user is entitled to via group membership, and return each one in a Set that can be iterated.
     * @param securityValidator Closure to filter graph definitions by matching security expressions
     * @return Set of matching GraphDefinition objects
     */
    Set getGraphDefinitions(Closure<Boolean> securityValidator) {
        this.getGraphDefinitions(null, null, securityValidator)
    }

    /**
     * Iterate the internal library of config graphs loaded from configured graph store, identify configurations
     * that a user is entitled to via group membership, and return each one in a Set that can be iterated.
     * @param filter Jexl expression to refine which graphs can be returned for each matching definition
     * @param securityValidator Closure to filter graph definitions by matching security expressions
     * @return Set of matching GraphDefinition objects
     */
    Set getGraphDefinitions(String filter, Closure<Boolean> securityValidator) {
        return this.getGraphDefinitions(filter, null, securityValidator)
    }

    /**
     * Iterate the internal library of config graphs loaded from configured graph store, identify configurations
     * that a user is entitled to via group membership, and return each one in a Set that can be iterated.
     * @param filter Jexl expression to refine which graphs can be returned for each matching definition
     * @param securityValidator Closure to filter graph definitions by matching security expressions
     * @param subpath Jexl expression to execute against the graph after filtering, i.e to extract only a portion of the tree
     * @return Set of matching GraphDefinition objects
     */
    Set getGraphDefinitions(String filter, String subpath, Closure<Boolean> securityValidator) {
        // Iterate and filter graph definitions using a developer closure
        return graphs.inject(new HashSet()) { Set result, GraphDefinition graphDefinition ->
            // Evaluate security expressions, and look for a match
            if(graphDefinition.securityExpressions.any { expression -> securityValidator.call(expression, graphDefinition) }) {
                // Is a filter provided?
                if(filter != null) {
                    // Execute the filter expression against the graph
                    if(graphDefinition.matches(filter)) {
                        // Add the graph to the output collection (with optional subpath traversing)
                        result.add(graphDefinition.get(subpath))
                    }
                }
                else {
                    // Add the graph to the output collection (with optional subpath traversing)
                    result.add(graphDefinition.get(subpath))
                }
            }
            return result
        }
    }

    /**
     * Helper method to convert a config object into JSON. Generally this can be used in conjunction
     * with replying to a portlet request.
     * @param source Source object to serialize into JSON
     * @return JSON string
     */
    static String convertToJson(Object source, boolean pretty = false) {
        def rawJson = JsonOutput.toJson(source)

        if(pretty) {
            return JsonOutput.prettyPrint(rawJson)
        }
        return rawJson
    }

}
