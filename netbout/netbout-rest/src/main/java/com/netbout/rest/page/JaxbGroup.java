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
package com.netbout.rest.page;

import com.ymock.util.Logger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB group of elements.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlRootElement(name = "group")
@XmlAccessorType(XmlAccessType.NONE)
public final class JaxbGroup {

    /**
     * Classes already created before.
     */
    private static final Map<String, Class> TYPES =
        new HashMap<String, Class>();

    /**
     * Collection of elements.
     */
    private final Collection group;

    /**
     * Public ctor, for JAXB.
     */
    public JaxbGroup() {
        throw new IllegalStateException("Illegal call");
    }

    /**
     * Private ctor.
     * @param grp Group of elements
     */
    protected JaxbGroup(final Collection grp) {
        this.group = grp;
    }

    /**
     * Public ctor.
     * @param grp Group of elements
     * @param name Name of parent element
     */
    public static Object build(final Collection grp, final String name) {
        synchronized (JaxbGroup.TYPES) {
            if (!JaxbGroup.TYPES.containsKey(name)) {
                JaxbGroup.TYPES.put(name, JaxbGroup.construct(name));
            }
            try {
                return JaxbGroup.TYPES.get(name)
                    .getDeclaredConstructor(Collection.class)
                    .newInstance(grp);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(ex);
            } catch (InstantiationException ex) {
                throw new IllegalStateException(ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * Get group of elements.
     * @return The collection
     */
    @XmlAnyElement(lax = true)
    @XmlMixed
    public final Collection getGroup() {
        return this.group;
    }

    /**
     * Construct new class.
     * @param name Name of root element
     */
    private static Class construct(final String name) {
        final ClassPool pool = ClassPool.getDefault();
        try {
            final CtClass ctc = pool.getAndRename(
                JaxbGroup.class.getName(),
                String.format("%s$%s", JaxbGroup.class.getName(), name)
            );
            final ClassFile file = ctc.getClassFile();
            final AnnotationsAttribute attribute =
                (AnnotationsAttribute) file.getAttribute(
                    AnnotationsAttribute.visibleTag
                );
            final Annotation annotation = attribute.getAnnotation(
                XmlRootElement.class.getName()
            );
            final StringMemberValue member =
                (StringMemberValue) annotation.getMemberValue("name");
            member.setValue(name);
            annotation.addMemberValue("name", member);
            final Class cls = ctc.toClass();
            Logger.debug(
                JaxbGroup.class,
                "#construct('%s'): class %s created",
                name,
                cls.getName()
            );
            return cls;
        } catch (javassist.NotFoundException ex) {
            throw new IllegalStateException(ex);
        } catch (javassist.CannotCompileException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
