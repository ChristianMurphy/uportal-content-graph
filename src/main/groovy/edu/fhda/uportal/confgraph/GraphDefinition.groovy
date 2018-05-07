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

import org.apache.commons.jexl3.JexlContext
import org.apache.commons.jexl3.JexlExpression
import org.apache.commons.jexl3.MapContext

/**
 * Intermediate type used for representing loaded config graph files.
 * @author mrapczynski , Foothill-De Anza College District, rapczynskimatthew@fhda.edu
 * @version 1.0
 */
class GraphDefinition implements Comparable<GraphDefinition> {

    Map<Object, Object> graph
    String id
    List<JexlExpression> securityExpressions = new ArrayList<JexlExpression>()

    GraphDefinition(String id, Map<Object, Object> source) {
        // Set instance members
        this.id = id

        // Is this graph marked disabled?
        if(source?.disabled == true) {
            throw new SkipGraphDefinitionException("Graph is marked disabled")
        }

        // Validate security expressions block is present in the graph
        if(!source.containsKey("securityExpressions")) {
            throw new NoSuchElementException("Missing securityExpressions collection")
        }

        // Unpack security expressions collections from source document
        securityExpressions = source
            .get("securityExpressions")
            .collect { String expression -> Jexl.cache.get(expression) }

        // Extract graph sub-object
        this.graph = source.get("graph") as Map
    }

    Object get(String expression) {
        if(expression == null) {
            return this.graph
        }
        
        // Create a Jexl context
        JexlContext context = new MapContext([ "self": this ])

        // Run the filter expression with this graph as a context input
        return Jexl
            .cache
            .get(expression)
            .evaluate(context) as Map
    }

    boolean matches(String expression) {
        // Create a Jexl context
        JexlContext context = new MapContext([ "self": this ])

        // Run the filter expression with this graph as a context input
        return Jexl
            .cache
            .get(expression)
            .evaluate(context)
    }

    @Override
    boolean equals(Object obj) {
        return id == (obj as GraphDefinition).id
    }

    @Override
    int compareTo(GraphDefinition other) {
        return id <=> other.id
    }

}
