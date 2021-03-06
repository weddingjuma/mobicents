/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jivesoftware.smackx.packet;

import java.util.*;

import org.jivesoftware.smack.packet.IQ;

/**
 * A DiscoverInfo IQ packet, which is used by XMPP clients to request and receive information 
 * to/from other XMPP entities.<p> 
 * 
 * The received information may contain one or more identities of the requested XMPP entity, and 
 * a list of supported features by the requested XMPP entity.
 *
 * @author Gaston Dombiak
 */
public class DiscoverInfo extends IQ {

    private List features = new ArrayList();
    private List identities = new ArrayList();
    private String node;

    /**
     * Adds a new feature to the discovered information.
     *
     * @param feature the discovered feature
     */
    public void addFeature(String feature) {
        addFeature(new DiscoverInfo.Feature(feature));
    }

    private void addFeature(Feature feature) {
        synchronized (features) {
            features.add(feature);
        }
    }

    /**
     * Returns the discovered features of an XMPP entity.
     *
     * @return an Iterator on the discovered features of an XMPP entity
     */
    Iterator getFeatures() {
        synchronized (features) {
            return Collections.unmodifiableList(new ArrayList(features)).iterator();
        }
    }

    /**
     * Adds a new identity of the requested entity to the discovered information.
     * 
     * @param identity the discovered entity's identity
     */
    public void addIdentity(Identity identity) {
        synchronized (identities) {
            identities.add(identity);
        }
    }

    /**
     * Returns the discovered identities of an XMPP entity.
     * 
     * @return an Iterator on the discoveted identities 
     */
    public Iterator getIdentities() {
        synchronized (identities) {
            return Collections.unmodifiableList(new ArrayList(identities)).iterator();
        }
    }

    /**
     * Returns the node attribute that supplements the 'jid' attribute. A node is merely 
     * something that is associated with a JID and for which the JID can provide information.<p> 
     * 
     * Node attributes SHOULD be used only when trying to provide or query information which 
     * is not directly addressable.
     *
     * @return the node attribute that supplements the 'jid' attribute
     */
    public String getNode() {
        return node;
    }

    /**
     * Sets the node attribute that supplements the 'jid' attribute. A node is merely 
     * something that is associated with a JID and for which the JID can provide information.<p> 
     * 
     * Node attributes SHOULD be used only when trying to provide or query information which 
     * is not directly addressable.
     * 
     * @param node the node attribute that supplements the 'jid' attribute
     */
    public void setNode(String node) {
        this.node = node;
    }

    /**
     * Returns true if the specified feature is part of the discovered information.
     * 
     * @param feature the feature to check
     * @return true if the requestes feature has been discovered
     */
    public boolean containsFeature(String feature) {
        for (Iterator it = getFeatures(); it.hasNext();) {
            if (feature.equals(((DiscoverInfo.Feature) it.next()).getVar()))
                return true;
        }
        return false;
    }

    public String getChildElementXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<query xmlns=\"http://jabber.org/protocol/disco#info\"");
        if (getNode() != null) {
            buf.append(" node=\"");
            buf.append(getNode());
            buf.append("\"");
        }
        buf.append(">");
        synchronized (identities) {
            for (int i = 0; i < identities.size(); i++) {
                Identity identity = (Identity) identities.get(i);
                buf.append(identity.toXML());
            }
        }
        synchronized (features) {
            for (int i = 0; i < features.size(); i++) {
                Feature feature = (Feature) features.get(i);
                buf.append(feature.toXML());
            }
        }
        // Add packet extensions, if any are defined.
        buf.append(getExtensionsXML());
        buf.append("</query>");
        return buf.toString();
    }

    /**
     * Represents the identity of a given XMPP entity. An entity may have many identities but all
     * the identities SHOULD have the same name.<p>
     * 
     * Refer to <a href="http://www.jabber.org/registrar/disco-categories.html">Jabber::Registrar</a>
     * in order to get the official registry of values for the <i>category</i> and <i>type</i> 
     * attributes.
     * 
     */
    public static class Identity {

        private String category;
        private String name;
        private String type;

        /**
         * Creates a new identity for an XMPP entity.
         * 
         * @param category the entity's category.
         * @param name the entity's name.
         */
        public Identity(String category, String name) {
            this.category = category;
            this.name = name;
        }

        /**
         * Returns the entity's category. To get the official registry of values for the 
         * 'category' attribute refer to <a href="http://www.jabber.org/registrar/disco-categories.html">Jabber::Registrar</a> 
         *
         * @return the entity's category.
         */
        public String getCategory() {
            return category;
        }

        /**
         * Returns the identity's name.
         *
         * @return the identity's name.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the entity's type. To get the official registry of values for the 
         * 'type' attribute refer to <a href="http://www.jabber.org/registrar/disco-categories.html">Jabber::Registrar</a> 
         *
         * @return the entity's type.
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the entity's type. To get the official registry of values for the 
         * 'type' attribute refer to <a href="http://www.jabber.org/registrar/disco-categories.html">Jabber::Registrar</a> 
         *
         * @param type the identity's type.
         */
        public void setType(String type) {
            this.type = type;
        }

        public String toXML() {
            StringBuffer buf = new StringBuffer();
            buf.append("<identity category=\"").append(category).append("\"");
            buf.append(" name=\"").append(name).append("\"");
            if (type != null) {
                buf.append(" type=\"").append(type).append("\"");
            }
            buf.append("/>");
            return buf.toString();
        }
    }

    /**
     * Represents the features offered by the item. This information helps requestors determine 
     * what actions are possible with regard to this item (registration, search, join, etc.) 
     * as well as specific feature types of interest, if any (e.g., for the purpose of feature 
     * negotiation).
     */
    public static class Feature {

        private String variable;

        /**
         * Creates a new feature offered by an XMPP entity or item.
         * 
         * @param variable the feature's variable.
         */
        public Feature(String variable) {
            this.variable = variable;
        }

        /**
         * Returns the feature's variable.
         *
         * @return the feature's variable.
         */
        public String getVar() {
            return variable;
        }

        public String toXML() {
            StringBuffer buf = new StringBuffer();
            buf.append("<feature var=\"").append(variable).append("\"/>");
            return buf.toString();
        }
    }
}