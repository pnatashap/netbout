/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.rest.jaxb;

import com.netbout.spi.Identity;
import com.netbout.spi.Participant;
import com.netbout.utils.AliasBuilder;
import java.net.URL;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Participant convertable to XML through JAXB.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "participant")
@XmlAccessorType(XmlAccessType.NONE)
public final class LongParticipant {

    /**
     * The bout.
     */
    private transient Participant participant;

    /**
     * URI builder.
     */
    private final transient UriBuilder builder;

    /**
     * The viewer of it.
     */
    private final transient Identity viewer;

    /**
     * Public ctor for JAXB.
     */
    public LongParticipant() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param dude The participant
     * @param bldr The builder
     * @param vwr The viewer
     */
    public LongParticipant(final Participant dude, final UriBuilder bldr,
        final Identity vwr) {
        this.participant = dude;
        this.builder = bldr;
        this.viewer = vwr;
    }

    /**
     * Get kick-off link.
     * @return The link
     */
    @XmlElement
    public Link getLink() {
        return new Link(
            "kickoff",
            this.builder.clone()
                .path("/kickoff")
                .queryParam("name", this.participant.identity().name())
                .build()
        );
    }

    /**
     * Get its identity.
     * @return The name
     */
    @XmlElement
    public String getIdentity() {
        return this.participant.identity().name().toString();
    }

    /**
     * Get his alias.
     * @return The alias
     */
    @XmlElement
    public String getAlias() {
        return new AliasBuilder(this.participant.identity()).build();
    }

    /**
     * Get its photo.
     * @return The photo
     */
    @XmlElement
    public URL getPhoto() {
        return this.participant.identity().photo();
    }

    /**
     * Is he confirmed?
     * @return Is it?
     */
    @XmlAttribute
    public Boolean isConfirmed() {
        return this.participant.confirmed();
    }

    /**
     * Is it me?
     * @return Is it?
     */
    @XmlAttribute
    public Boolean isMe() {
        return this.participant.identity().equals(this.viewer);
    }

}