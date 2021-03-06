/*
 * Copyright (c) 2012, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eel.kitchen.jsonschema.ref;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import org.eel.kitchen.jsonschema.main.JsonSchemaException;
import org.eel.kitchen.jsonschema.report.Domain;
import org.eel.kitchen.jsonschema.report.Message;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Representation of a JSON Reference
 *
 * <p><a href="http://tools.ietf.org/html/draft-pbryan-zyp-json-ref-03">JSON
 * Reference</a>, currently a draft, is a way to define a path within a JSON
 * document.</p>
 *
 * <p>To quote the draft, "A JSON Reference is a JSON object, which contains
 * a member named "$ref", which has a JSON string value." This string value
 * must be a URI. Example:</p>
 *
 * <pre>
 *     {
 *         "$ref": "http://example.com/example.json#/foo/bar"
 *     }
 * </pre>
 *
 * <p>This class is used in a more general way than the draft. It is also used
 * as a backing class for schema identifiers.</p>
 *
 * <p>The implementation is a wrapper over Java's {@link URI}, with the
 * following differences:</p>
 *
 * <ul>
 *     <li>all URIs are normalized from the get go;</li>
 *     <li>an empty fragment is equivalent to no fragment at all,
 *     and stands for a root JSON Pointer;</li>
 *     <li>a reference is taken to be absolute if the underlying URI is
 *     absolute <i>and</i> it has no fragment, or an empty fragment.</li>
 * </ul>
 *
 * <p>This class is thread safe and immutable.</p>
 */

public final class JsonRef
{
    /**
     * URI with only an empty fragment part
     */
    private static final URI HASHONLY_URI = URI.create("#");

    /**
     * Empty URI
     */
    private static final URI EMPTY_URI = URI.create("");

    /**
     * An empty JSON Reference
     */
    private static final JsonRef EMPTY = new JsonRef(EMPTY_URI);

    /**
     * The URI, as provided by the input, with an appended empty fragment if
     * no fragment was provided
     */
    private final URI uri;

    /**
     * The locator for this fragment
     */
    private final URI locator;

    /**
     * The fragment of this JSON Reference
     */
    private final JsonFragment fragment;

    private final int hashCode;

    /**
     * The main constructor, which is private by design
     *
     * <p>If the provided URI has no fragment, then an empty one is appended.
     * </p>
     *
     * @param uri Input URI
     */
    private JsonRef(final URI uri)
    {
        try {
            locator = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), "");
            this.uri = uri.getFragment() == null ? locator : uri;
            fragment = JsonFragment.fromFragment(this.uri.getFragment());
            hashCode = uri.hashCode();
        } catch (URISyntaxException e) {
            throw new RuntimeException("WTF??", e);
        }
    }

    /**
     * Build a JSON Reference from a URI
     *
     * @see #JsonRef(URI)
     *
     * @param uri the provided URI
     * @return the JSON Reference
     * @throws NullPointerException the provided URI is null
     */
    public static JsonRef fromURI(final URI uri)
    {
        Preconditions.checkNotNull(uri, "URI must not be null");

        final URI normalized = uri.normalize();

        if (HASHONLY_URI.equals(normalized) || EMPTY_URI.equals(normalized))
            return EMPTY;

        return new JsonRef(normalized);
    }

    /**
     * Build a JSON Reference from a string input
     *
     * @param s the string
     * @return the reference
     * @throws JsonSchemaException string is not a valid URI
     * @throws NullPointerException provided string is null
     */
    public static JsonRef fromString(final String s)
        throws JsonSchemaException
    {
        Preconditions.checkNotNull(s, "string must not be null");

        try {
            return fromURI(new URI(s));
        } catch (URISyntaxException e) {
            final Message.Builder msg = Domain.REF_RESOLVING.newMessage()
                .setKeyword("N/A").addInfo("uri", s).setMessage("invalid URI");
            throw new JsonSchemaException(msg.build(), e);
        }
    }

    /**
     * Build a JSON Reference from a {@link JsonNode}
     *
     * <p>If the node is not textual, this returns an empty reference.
     * Otherwise, it calls {@link #fromString(String)} with this node's text
     * value.</p>
     *
     * @param node the node
     * @return the reference
     * @throws JsonSchemaException see {@link #fromString(String)}
     * @throws NullPointerException provided node is null
     */
    public static JsonRef fromNode(final JsonNode node)
        throws JsonSchemaException
    {
        Preconditions.checkNotNull(node, "node must not be null");

        return node.isTextual() ? fromString(node.textValue()) : EMPTY;
    }

    /**
     * Return an empty reference
     *
     * <p>An empty reference is a reference which only has an empty fragment.
     * </p>
     *
     * @return see above
     */
    public static JsonRef emptyRef()
    {
        return EMPTY;
    }

    /**
     * Tell whether this reference is an absolute reference
     *
     * <p>A JSON Reference is considered absolute iif the underlying URI is
     * itself absolute <b>and</b> it has an empty, or no, fragment part.</p>
     *
     * @return see above
     */
    public boolean isAbsolute()
    {
        return uri.isAbsolute() && fragment.isEmpty();
    }

    /**
     * Resolve this reference against another reference
     *
     * <p>Note: {@code jar} URIs need to be special cased, see {@link
     * #resolveJarURI(JsonRef)}.</p>
     *
     * @param other the reference to resolve
     * @return the resolved reference
     */
    public JsonRef resolve(final JsonRef other)
    {
        if (other.uri.isAbsolute())
            return other;

        final URI targetURI = "jar".equals(uri.getScheme())
            ? resolveJarURI(other) : uri.resolve(other.uri);
        return new JsonRef(targetURI);
    }

    /**
     * Return this JSON Reference's locator
     *
     * <p>The locator of a fragment is the URI with an empty fragment.</p>
     *
     * @return an URI
     */
    public URI getRootAsURI()
    {
        return locator;
    }

    /**
     * Return this JSON Reference's fragment
     *
     * @return the fragment
     */
    public JsonFragment getFragment()
    {
        return fragment;
    }

    /**
     * Tell whether the current JSON Reference contains another
     *
     * <p>This is considered true iif both references have the same locator,
     * in other words, if they differ only by their fragment part.</p>
     *
     * @param other the other reference
     * @return see above
     */
    public boolean contains(final JsonRef other)
    {
        return locator.equals(other.locator);
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
            return false;
        if (this == obj)
            return true;

        if (getClass() != obj.getClass())
            return false;

        final JsonRef that = (JsonRef) obj;
        return uri.equals(that.uri);
    }

    @Override
    public String toString()
    {
        return uri.toString();
    }

    /**
     * Resolve a relative reference against a {@code jar} URI
     *
     * <p>Unfortunately, {@link URI#resolve(URI)} does not work for these,
     * because of the way JAR URIs are built. An example JAR URI is:</p>
     *
     * <pre>
     *     jar:file:/path/to/my.jar!/jar/entry.json#fragmentHere
     * </pre>
     *
     * <p>As per URI rules, this URI has no host and no path, just a "scheme
     * specific part" containing everyting after the scheme. Which means that
     * using {@code .resolve(x)} on such a URI for whatever URI {@code x} will
     * return... Yes, {@code x}. This is how URI resolution is supposed to work.
     * </p>
     *
     * <p>We therefore handle these specially in this method:</p>
     *
     * <ul>
     *     <li>we are guaranteed that if we enter here, the other URI is
     *     relative: it has no scheme part, it is therefore a path followed by
     *     a fragment (if any);</li>
     *     <li>we therefore extract whatever is after the {@code !} (we are
     *     guaranteed that there is one: we cannot enter this whole class if the
     *     URI did not resolve successfully in the first place), make a URI out
     *     of it and use classic URI resolution rules to resolve that extracted
     *     path against the other URI;</li>
     *     <li>we then rebuild the URI with this new path.</li>
     * </ul>
     *
     * <p>This means that, for instance, resolving {@code ../x.json#} against
     * the URI above will lead to {@code jar:file:/path/to/my.jar!/x.json#}.</p>
     *
     * <p>Sick. That's the price to pay for supporting the {@code jar} scheme in
     * the first place :(</p>
     *
     * @param other the URI to resolve against
     * @return the newly, specially crafted URI
     */
    private URI resolveJarURI(final JsonRef other)
    {
        final String str = uri.toString();

        final int idx = str.indexOf('!');
        final String jarPath = str.substring(0, idx + 1);
        final String sourceEntry = str.substring(idx + 1);

        final String targetEntry
            = URI.create(sourceEntry).resolve(other.uri).toString();
        return URI.create(jarPath + targetEntry);
    }
}
