/*
 * Copyright (c) 2011, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eel.kitchen.util;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public final class JasonHelper
{
    private static final Logger logger
        = LoggerFactory.getLogger(JasonHelper.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonNode load(final String resource)
        throws IOException
    {
        final String realResource = resource.startsWith("/")
            ? resource : '/' + resource;

        final InputStream in
            = JasonHelper.class.getResourceAsStream(realResource);

        return mapper.readTree(in);
    }

    public static String getNodeType(final JsonNode node)
    {
        if (node.isArray())
            return "array";
        if (node.isObject())
            return "object";
        if (node.isTextual())
            return "string";
        if (node.isNumber())
            return node.isIntegralNumber() ? "integer" : "number";
        if (node.isBoolean())
            return "boolean";
        if (node.isNull())
            return "null";

        logger.warn("Could not determine node type??? Dump follows");
        logger.warn(node.toString());
        return "unknown";
    }
}