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

package org.restlet.ext.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.Edition;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML representation based on a DOM document. DOM is a standard XML object
 * model defined by the W3C.
 * 
 * @author Jerome Louvel
 */
public class DomRepresentation extends XmlRepresentation {
    /** The wrapped DOM document. */
    private volatile Document document;

    /** Indicates if the XML serialization should be indented. */
    private volatile boolean indenting;

    /** The source XML representation. */
    private volatile Representation xmlRepresentation;

    /**
     * Default constructor. Uses the {@link MediaType#TEXT_XML} media type.
     */
    public DomRepresentation() throws IOException {
        this(MediaType.TEXT_XML);
    }

    /**
     * Constructor for an empty document.
     * 
     * @param mediaType
     *            The representation's media type.
     */
    public DomRepresentation(MediaType mediaType) throws IOException {
        super(mediaType);
        this.document = getDocumentBuilder().newDocument();
    }

    /**
     * Constructor from an existing DOM document.
     * 
     * @param mediaType
     *            The representation's media type.
     * @param xmlDocument
     *            The source DOM document.
     */
    public DomRepresentation(MediaType mediaType, Document xmlDocument) {
        super(mediaType);
        this.document = xmlDocument;
    }

    /**
     * Constructor.
     * 
     * @param xmlRepresentation
     *            A source XML representation to parse.
     */
    public DomRepresentation(Representation xmlRepresentation) {
        super((xmlRepresentation == null) ? null : xmlRepresentation
                .getMediaType());
        this.setAvailable(xmlRepresentation.isAvailable());
        this.xmlRepresentation = xmlRepresentation;
    }


    /**
     * Returns the wrapped DOM document. If no document is defined yet, it
     * attempts to parse the XML representation eventually given at construction
     * time. Otherwise, it just creates a new document.
     * 
     * @return The wrapped DOM document.
     */
    @Override
    public Document getDocument() throws IOException {
        if (this.document == null) {
            if (this.xmlRepresentation != null) {
                try {
                    this.document = getDocumentBuilder()
                            .parse(getInputSource());
                } catch (SAXException se) {
                    throw new IOException(
                            "Couldn't read the XML representation. "
                                    + se.getMessage());
                }
            } else {
                this.document = getDocumentBuilder().newDocument();
            }
        }

        return this.document;
    }


    @Override
    public InputSource getInputSource() throws IOException {
        if (this.xmlRepresentation.isAvailable()) {
            return new InputSource(this.xmlRepresentation.getStream());
        }
        return new InputSource((InputStream) null);
    }

    /**
     * Indicates if the XML serialization should be indented. False by default.
     * 
     * @return True if the XML serialization should be indented.
     * @deprecated Use {@link #isIndenting()} instead.
     */
    @Deprecated
    public boolean isIndent() {
        return indenting;
    }

    /**
     * Indicates if the XML serialization should be indented. False by default.
     * 
     * @return True if the XML serialization should be indented.
     */
    public boolean isIndenting() {
        return isIndent();
    }

    /**
     * Releases the wrapped DOM document and the source XML representation if
     * they have been defined.
     */
    @Override
    public void release() {
        setDocument(null);

        if (this.xmlRepresentation != null) {
            this.xmlRepresentation.release();
        }

        super.release();
    }

    /**
     * Sets the wrapped DOM document.
     * 
     * @param dom
     *            The wrapped DOM document.
     */
    public void setDocument(Document dom) {
        this.document = dom;
    }

    /**
     * Indicates if the XML serialization should be indented.
     * 
     * @param indenting
     *            True if the XML serialization should be indented.
     * @deprecated Use {@link #setIndenting(boolean)} instead.
     */
    @Deprecated
    public void setIndent(boolean indenting) {
        this.indenting = indenting;
    }

    /**
     * Indicates if the XML serialization should be indented.
     * 
     * @param indenting
     *            True if the XML serialization should be indented.
     */
    public void setIndenting(boolean indenting) {
        setIndent(indenting);
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (Edition.CURRENT == Edition.ANDROID) {
            throw new UnsupportedOperationException(
                    "Instances of DomRepresentation cannot be written at this time.");
        }
    }
}
