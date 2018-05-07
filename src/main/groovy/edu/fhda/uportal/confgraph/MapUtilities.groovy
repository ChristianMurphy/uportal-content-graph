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

/**
 * Utility methods for working with java.util.Map collections.
 * @author mrapczynski , Foothill-De Anza College District, rapczynskimatthew@fhda.edu
 * @version 1.0
 */
class MapUtilities {

    /**
     * Awesome Groovy deep-merge function for multiple Map objects. https://stackoverflow.com/a/27476077
     * Tweaked by @mrapczynski to add support for merging java.util.List objects
     * @param sources
     * @return
     */
    static Map deepMerge(Map... sources) {
        // If no sources provided, return an empty map
        if (sources.length == 0) {
            return [:]
        }

        // If only one source is provided, then skip deep merge
        else if (sources.length == 1) {
            return sources[0]
        }

        // Begin recursive deep merge
        else {
            def result = [:]

            // Iterate over each source map
            sources.each { map ->
                // Iterate over LH key/value pairs
                map.each { key, value ->
                    // Check if the left and right sides are both java.util.Lists and can be appended together
                    if(result[key] instanceof List && value instanceof List) {
                        (result[key] as List).addAll(value)
                    }
                    else {
                        result[key] = result[key] instanceof Map ? deepMerge(result[key], value) : value
                    }
                }
            }

            return result
        }
    }

}
