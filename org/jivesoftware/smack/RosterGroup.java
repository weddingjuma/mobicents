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

package org.jivesoftware.smack;

import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.filter.PacketIDFilter;

import java.util.*;

/**
 * A group of roster entries.
 *
 * @see Roster#getGroup(String)
 * @author Matt Tucker
 */
public class RosterGroup {

    private String name;
    private XMPPConnection connection;
    private List entries;

    /**
     * Creates a new roster group instance.
     *
     * @param name the name of the group.
     * @param connection the connection the group belongs to.
     */
    RosterGroup(String name, XMPPConnection connection) {
        this.name = name;
        this.connection = connection;
        entries = new ArrayList();
    }

    /**
     * Returns the name of the group.
     *
     * @return the name of the group.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the group. Changing the group's name is like moving all the group entries
     * of the group to a new group specified by the new name. Since this group won't have entries 
     * it will be removed from the roster. This means that all the references to this object will 
     * be invalid and will need to be updated to the new group specified by the new name.
     *
     * @param name the name of the group.
     */
    public void setName(String name) {
        synchronized (entries) {
            for (int i=0; i<entries.size(); i++) {
                RosterPacket packet = new RosterPacket();
                packet.setType(IQ.Type.SET);
                RosterEntry entry = (RosterEntry)entries.get(i);
                RosterPacket.Item item = RosterEntry.toRosterItem(entry);
                item.removeGroupName(this.name);
                item.addGroupName(name);
                packet.addRosterItem(item);
                connection.sendPacket(packet);
            }
        }
    }

    /**
     * Returns the number of entries in the group.
     *
     * @return the number of entries in the group.
     */
    public int getEntryCount() {
        synchronized (entries) {
            return entries.size();
        }
    }

    /**
     * Returns an iterator for the entries in the group.
     *
     * @return an iterator for the entries in the group.
     */
    public Iterator getEntries() {
        synchronized (entries) {
            return Collections.unmodifiableList(new ArrayList(entries)).iterator();
        }
    }

    /**
     * Returns the roster entry associated with the given XMPP address or
     * <tt>null</tt> if the user is not an entry in the group.
     *
     * @param user the XMPP address of the user (eg "jsmith@example.com").
     * @return the roster entry or <tt>null</tt> if it does not exist in the group.
     */
    public RosterEntry getEntry(String user) {
        if (user == null) {
            return null;
        }
        // Roster entries never include a resource so remove the resource
        // if it's a part of the XMPP address.
        user = StringUtils.parseBareAddress(user);
        String userLowerCase = user.toLowerCase();
        synchronized (entries) {
            for (Iterator i=entries.iterator(); i.hasNext(); ) {
                RosterEntry entry = (RosterEntry)i.next();
                if (entry.getUser().equals(userLowerCase)) {
                    return entry;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if the specified entry is part of this group.
     *
     * @param entry a roster entry.
     * @return true if the entry is part of this group.
     */
    public boolean contains(RosterEntry entry) {
        synchronized (entries) {
            return entries.contains(entry);
        }
    }

    /**
     * Returns true if the specified XMPP address is an entry in this group.
     *
     * @param user the XMPP address of the user.
     * @return true if the XMPP address is an entry in this group.
     */
    public boolean contains(String user) {
        return getEntry(user) != null;
    }

    /**
     * Adds a roster entry to this group. If the entry was unfiled then it will be removed from 
     * the unfiled list and will be added to this group.
     *
     * @param entry a roster entry.
     * @throws XMPPException if an error occured while trying to add the entry to the group.
     */
    public void addEntry(RosterEntry entry) throws XMPPException {
        PacketCollector collector = null;
        // Only add the entry if it isn't already in the list.
        synchronized (entries) {
            if (!entries.contains(entry)) {
                RosterPacket packet = new RosterPacket();
                packet.setType(IQ.Type.SET);
                RosterPacket.Item item = RosterEntry.toRosterItem(entry);
                item.addGroupName(getName());
                packet.addRosterItem(item);
                // Wait up to a certain number of seconds for a reply from the server.
                collector = connection
                        .createPacketCollector(new PacketIDFilter(packet.getPacketID()));
                connection.sendPacket(packet);
            }
        }
        if (collector != null) {
            IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
            collector.cancel();
            if (response == null) {
                throw new XMPPException("No response from the server.");
            }
            // If the server replied with an error, throw an exception.
            else if (response.getType() == IQ.Type.ERROR) {
                throw new XMPPException(response.getError());
            }
            // Add the new entry to the group since the server processed the request successfully
            addEntryLocal(entry);
        }
    }

    /**
     * Removes a roster entry from this group. If the entry does not belong to any other group 
     * then it will be considered as unfiled, therefore it will be added to the list of unfiled 
     * entries.
     *
     * @param entry a roster entry.
     * @throws XMPPException if an error occured while trying to remove the entry from the group. 
     */
    public void removeEntry(RosterEntry entry) throws XMPPException {
        PacketCollector collector = null;
        // Only remove the entry if it's in the entry list.
        // Remove the entry locally, if we wait for RosterPacketListenerprocess>>Packet(Packet)
        // to take place the entry will exist in the group until a packet is received from the 
        // server.
        synchronized (entries) {
            if (entries.contains(entry)) {
                RosterPacket packet = new RosterPacket();
                packet.setType(IQ.Type.SET);
                RosterPacket.Item item = RosterEntry.toRosterItem(entry);
                item.removeGroupName(this.getName());
                packet.addRosterItem(item);
                // Wait up to a certain number of seconds for a reply from the server.
                collector = connection
                        .createPacketCollector(new PacketIDFilter(packet.getPacketID()));
                connection.sendPacket(packet);
            }
        }
        if (collector != null) {
            IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
            collector.cancel();
            if (response == null) {
                throw new XMPPException("No response from the server.");
            }
            // If the server replied with an error, throw an exception.
            else if (response.getType() == IQ.Type.ERROR) {
                throw new XMPPException(response.getError());
            }
            // Remove the entry locally since the server processed the request successfully
            removeEntryLocal(entry);
        }
    }

    void addEntryLocal(RosterEntry entry) {
        // Only add the entry if it isn't already in the list.
        synchronized (entries) {
            entries.remove(entry);
            entries.add(entry);
        }
    }

    void removeEntryLocal(RosterEntry entry) {
         // Only remove the entry if it's in the entry list.
        synchronized (entries) {
            if (entries.contains(entry)) {
                entries.remove(entry);
            }
        }
    }
}