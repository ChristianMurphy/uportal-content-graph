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

package edu.fhda.uportal.confgraph.loader

import edu.fhda.uportal.confgraph.GraphDefinition
import edu.fhda.uportal.confgraph.SkipGraphDefinitionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml

/**
 * @author mrapczynski , Foothill-De Anza College District, rapczynskimatthew@fhda.edu
 * @version 1.0
 */
class FilesystemGraphStore implements GraphStore {

    private static final Logger logger = LoggerFactory.getLogger(FilesystemGraphStore.class)

    // Create a Yaml parser instance
    private static final Yaml yaml = new Yaml()

    // Private properties
    private final File sourceDirectory

    FilesystemGraphStore(String sourceDirectory) {
        this.sourceDirectory = new File(sourceDirectory)
    }

    void load(Closure callback) {
        try {
            // Iterate through files in source directory
            this.sourceDirectory.eachFileRecurse { file ->
                // Verify file extension ends in .yaml
                if(file.name =~ /.*\.yaml/) {
                    logger.debug("Parsing ${file.name}")

                    // Parse content as YAML
                    Map graph = parseYamlDocument(file)

                    // Was parsing successful?
                    if(graph != null) {
                        // Create graph definition
                        GraphDefinition definition = createGraphDefinition(file.name, graph)

                        // Was graph creation successful?
                        if(definition != null) {
                            // Call closure with parsed graph
                            callback.call(definition)

                            logger.debug("Successfully created graph definition file=${file}")
                        }
                    }
                }
            }

            logger.debug("Successfully completed load of config graph objects source=${this.sourceDirectory}")
        }
        catch(Exception error) {
            logger.error("Failed to list graphs from filesystem file=${this.sourceDirectory}", error)
        }
    }

    static GraphDefinition createGraphDefinition(String id, Map source) {
        try {
            return new GraphDefinition(id, source)
        }
        catch(SkipGraphDefinitionException warning) {
            logger.warn("Skipping graph definition for id=${id} reason=${warning.message}")
            return null
        }
        catch(Exception error) {
            logger.error("Failed to create graph definition for id=${id}", error)
            return null
        }
    }

    static Map parseYamlDocument(File sourceFile) {
        try {
            return sourceFile.withReader { reader ->
                return yaml.load(reader) as Map
            }
        }
        catch(Exception error) {
            logger.error("Failed to parse YAML document file=${sourceFile}", error)
            return null
        }
    }
    
}
