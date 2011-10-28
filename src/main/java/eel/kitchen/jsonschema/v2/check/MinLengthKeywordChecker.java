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

package eel.kitchen.jsonschema.v2.check;

//TODO: implement
public final class MinLengthKeywordChecker
    extends UnsupportedKeywordChecker
//    extends SingleTypeKeywordChecker
{
    public MinLengthKeywordChecker()
    {
        super("minLength");
        //super("minLength", NodeType.INTEGER);
    }

//    @Override
//    public boolean validate(final JsonNode schema)
//    {
//        if (!super.validate(schema))
//            return false;
//
//        final JsonNode node = schema.get("minLength");
//
//        if (!node.isInt()) {
//            messages.add("minLength is too large");
//            return false;
//        }
//
//        if (node.getIntValue() < 0) {
//            messages.add("minLength is negative");
//            return false;
//        }
//
//        return true;
//    }
}