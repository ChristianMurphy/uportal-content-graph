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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlEngine
import org.apache.commons.jexl3.JexlExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Support class with static instances of Jexl objects for handling expressions.
 * @author mrapczynski , Foothill-De Anza College District, rapczynskimatthew@fhda.edu
 * @version 1.0
 */
class Jexl {

    private static final Logger logger = LoggerFactory.getLogger(Jexl.class)

    private static final Map<String, Object> customNamespaces = [
        "fhda": new JexlFunctions()
    ]

    // Objects for managing the Jexl expression engine used to apply security rules to config graphs
    private static final JexlEngine engine = new JexlBuilder()
        .namespaces(customNamespaces)
        .create()

    // Guava cache for retaining compiled expressions in memory
    public static final LoadingCache<String, JexlExpression> cache = CacheBuilder.newBuilder()
        .maximumSize(256)
        .build(
        new CacheLoader<String, JexlExpression>() {
            JexlExpression load(String expression) throws Exception {
                logger.debug("Compiling Jexl expression ${expression}")
                return engine.createExpression(expression)
            }
        })

}
