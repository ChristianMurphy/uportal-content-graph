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

import edu.fhda.uportal.confgraph.loader.FilesystemGraphStore
import edu.fhda.uportal.confgraph.loader.GraphStore
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
/**
 * Test cases for edu.fhda.uportal.confgraph.ConfigGraphService
 * @author mrapczynski , Foothill-De Anza College District, rapczynskimatthew@fhda.edu
 * @version 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ConfigGraphServiceTests {

    // Create file loader
    static final GraphStore store = new FilesystemGraphStore("samples")

    // Create config graph service
    static final ConfigGraphService service = new ConfigGraphService(store)

    @Test
    void test001_BuildingCompleteGraph_Round1() {
        // Fetch all content
        def content = service.buildGraph { expression, graphDefinition -> true }

        // Serialize to JSON
        println service.convertToJson(content, true)
    }

    @Test
    void test002_FilteredGraph() {
        // Fetch all content
        def content = service.buildGraph("self.graph.content != null", { expression, graphDefinition -> true })

        // Serialize to JSON (pretty)
        println service.convertToJson(content, true)
    }

    @Test
    void test003_SelectSubgraph() {
        // TODO: Deep selectors not quite ready. Needs more work.

        // Fetch all content
        def content = service.buildGraph("self.graph.content != null", "self.graph.content['app.email.exchange']", { expression, graphDefinition -> true })

        // Serialize to JSON (pretty)
        println service.convertToJson(content, true)
    }

}
