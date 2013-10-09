/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.db;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.Utc;
import com.jcabi.jdbc.VoidHandler;
import com.jcabi.urn.URN;
import com.netbout.spi.cpa.Farm;
import com.netbout.spi.cpa.Operation;
import java.sql.SQLException;
import java.util.List;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Manipulations with bills.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Farm
public final class BillFarm {

    /**
     * Save a collection of incoming bills, from BUS.
     * @param lines Text forms of them
     * @throws SQLException If fails
     * @checkstyle MagicNumber (30 lines)
     */
    @Operation("save-bills")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void saveBills(final List<String> lines) throws SQLException {
        final JdbcSession session = new JdbcSession(Database.source());
        session.autocommit(false);
        final JdbcSession.Handler<Void> handler = new VoidHandler();
        for (String line : lines) {
            final String[] parts = line.split("[ ]+");
            Long bout = null;
            if (!"null".equals(parts[4])) {
                bout = Long.valueOf(parts[4].replaceAll("[^\\d]+", ""));
            }
            session
                // @checkstyle LineLength (1 line)
                .sql("INSERT INTO bill (date, mnemo, helper, msec, bout) VALUES (?, ?, ?, ?, ?)")
                .set(
                    new Utc(
                        ISODateTimeFormat.dateTime()
                            .parseDateTime(parts[0])
                            .toDate()
                    )
                )
                .set(parts[1])
                .set(URN.create(parts[2]))
                .set(Long.valueOf(parts[3]))
                .set(bout)
                .insert(handler);
        }
        session.commit();
    }

}