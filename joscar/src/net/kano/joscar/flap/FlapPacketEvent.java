/*
 *  Copyright (c) 2002, The Joust Project
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without 
 *  modification, are permitted provided that the following conditions 
 *  are met:
 *
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution. 
 *  - Neither the name of the Joust Project nor the names of its
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 *  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 *  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  File created by keith @ Feb 14, 2003
 *
 */

package net.kano.joscar.flap;

import net.kano.joscar.DefensiveTools;

/**
 * An event fired when a FLAP packet is received on a FLAP connection.
 */
public class FlapPacketEvent {
    /**
     * The FLAP processor on which this packet was received.
     */
    private final FlapProcessor flapProcessor;

    /**
     * The FLAP command, if any, generated from this packet.
     */
    private final FlapCommand flapCommand;

    /**
     * The FLAP packet received.
     */
    private final FlapPacket flapPacket;

    /**
     * Creates a new FLAP packet event object, a copy of the given event object.
     * @param other the FLAP packet event to copy
     */
    protected FlapPacketEvent(FlapPacketEvent other) {
        DefensiveTools.checkNull(other, "other");

        flapProcessor = other.flapProcessor;
        flapCommand = other.flapCommand;
        flapPacket = other.flapPacket;
    }

    /**
     * Creates a new FLAP packet event representing the receival of the given
     * packet by the given FLAP processor, along with the given FLAP
     * command generated from that packet.
     *
     * @param conn the FLAP connection that received this packet
     * @param packet the packet received
     * @param command the FLAP command generated by this packet
     */
    protected FlapPacketEvent(FlapProcessor conn, FlapPacket packet,
            FlapCommand command) {
        DefensiveTools.checkNull(conn, "conn");
        DefensiveTools.checkNull(packet, "packet");

        this.flapProcessor = conn;
        this.flapPacket = packet;
        this.flapCommand = command;
    }

    /**
     * The FLAP connection that received a packet.
     * @return the FLAP connection that received the packet that caused this
     *         event
     */
    public final FlapProcessor getFlapProcessor() {
        return flapProcessor;
    }

    /**
     * The FLAP packet that was received.
     * @return the received FLAP packet
     */
    public final FlapPacket getFlapPacket() {
        return flapPacket;
    }

    /**
     * The FLAP command generated from the received FLAP packet.
     * @return the FLAP command associated with this event
     */
    public final FlapCommand getFlapCommand() {
        return flapCommand;
    }
}
