/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.http.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.restlet.engine.io.IoUtils;

// [excludes gwt]
/**
 * Buffered input stream that prevent the underlying stream from being closed.
 * 
 * @author Jerome Louvel
 */
public class InboundStream extends BufferedInputStream {

    /**
     * Constructor.
     * 
     * @param source
     *            The source input stream.
     */
    public InboundStream(InputStream source) {
        super(source, IoUtils.BUFFER_SIZE);
    }

    /**
     * Constructor.
     * 
     * @param source
     *            The source input stream.
     * @param size
     *            the buffer size.
     */
    public InboundStream(InputStream source, int size) {
        super(source, size);
    }

    @Override
    public void close() throws IOException {
    }
}
