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

package eel.kitchen.jsonschema.v2.schema;

import eel.kitchen.jsonschema.v2.instance.Instance;
import eel.kitchen.jsonschema.v2.keyword.KeywordValidator;
import eel.kitchen.jsonschema.v2.keyword.KeywordValidatorProvider;
import eel.kitchen.jsonschema.v2.keyword.ValidationStatus;
import eel.kitchen.util.NodeType;
import org.codehaus.jackson.JsonNode;

import java.util.Set;

public final class SingleSchema
    extends AbstractSchema
{
    private static final KeywordValidatorProvider validatorProvider
        = KeywordValidatorProvider.getInstance();

    private final SchemaFactory factory;
    private final JsonNode schemaNode;

    private PathProvider pathProvider = ScalarPathProvider.getInstance();

    public SingleSchema(final SchemaFactory factory, final JsonNode schemaNode)
    {
        this.factory = factory;
        this.schemaNode = schemaNode;
    }

    @Override
    public Schema getSchema(final String path)
    {
        return factory.getSchema(pathProvider.getSchema(path));
    }

    @Override
    public boolean validate(final Instance instance)
    {
        final NodeType instanceType = instance.getType();

        final JsonNode node = instance.getRawInstance();

        final Set<KeywordValidator> validators
            = validatorProvider.getValidators(schemaNode, instanceType);

        boolean ret = true;

        for (final KeywordValidator validator: validators) {
            ValidationStatus status = validator.validate(node);
            if (status == ValidationStatus.DUNNO) {
                ret = validator.getNextSchema().validate(instance);
                status = ret ? ValidationStatus.SUCCESS
                    : ValidationStatus.FAILURE;
            }
            if (status != ValidationStatus.SUCCESS) {
                messages.addAll(validator.getMessages());
                ret = false;
            }
        }

        if (!ret)
            return false;

        pathProvider = instanceType == NodeType.ARRAY
            ? new ArrayPathProvider(node)
            : new ObjectPathProvider(node);

        Schema schema;

        for (final Instance child: instance) {
            schema = getSchema(child.getPathElement());
            if (schema.validate(child))
                continue;
            messages.addAll(schema.getMessages());
            ret = false;
        }

        return ret;
    }
}