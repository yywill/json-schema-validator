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

package org.eel.kitchen.jsonschema.uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Default URI downloader
 *
 * <p>{@link URL}'s API doc guarantees that an implementation can handle the
 * following schemes: http, https, ftp, file and jar.</p>
 *
 * <p>We choose however not to provide https for certificate handling reasons,
 * which is a pain to get right.</p>
 *
 * @see URL#openStream()
 */
public final class DefaultURIDownloader
    implements URIDownloader
{
    private static final URIDownloader instance
        = new DefaultURIDownloader();

    private DefaultURIDownloader()
    {
    }

    public static URIDownloader getInstance()
    {
        return instance;
    }

    @Override
    public InputStream fetch(final URI source)
        throws IOException
    {
        return source.toURL().openStream();
    }
}
