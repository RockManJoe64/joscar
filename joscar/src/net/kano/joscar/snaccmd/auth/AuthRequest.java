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
 *  File created by keith @ Feb 18, 2003
 *
 */

package net.kano.joscar.snaccmd.auth;

import net.kano.joscar.ByteBlock;
import net.kano.joscar.DefensiveTools;
import net.kano.joscar.flapcmd.SnacPacket;
import net.kano.joscar.tlv.Tlv;
import net.kano.joscar.tlv.ImmutableTlvChain;
import net.kano.joscar.tlv.TlvChain;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * A SNAC command used to log into the OSCAR server.
 * <br>
 * <br>
 * For those interested, authorization is done by sending an MD5 hash of the
 * string formed by concatenating the {@linkplain KeyResponse#getKey
 * authorization key}, the user's password, and the string "AOL Instant
 * Messenger (SM)". This way the user's password is never sent over an OSCAR
 * connection. I wonder why they didn't just use SSL or SSH, though.
 *
 * @snac.src client
 * @snac.cmd 0x17 0x02
 * @see AuthResponse
 */
public class AuthRequest extends AuthCommand {
    /** A TLV type containing the user's screen name. */
    private static final int TYPE_SN = 0x0001;
    /** A TLV type containing the user's two-letter country code. */
    private static final int TYPE_COUNTRY = 0x000e;
    /** A TLV type containing the user's two-letter language code. */
    private static final int TYPE_LANG = 0x000f;
    /** A TLV type containing the user's password, encrypted. */
    private static final int TYPE_ENCPASS = 0x0025;

    /**
     * Encrypts the given password with the given key into a format suitable
     * for sending in an auth request packet.
     *
     * @param pass the user's password
     * @param key a "key" provided by the server
     * @return the user's password, encrypted
     */
    private static byte[] encryptPassword(String pass, ByteBlock key) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException impossible) { return null; }

        try {
            md5.update(key.toByteArray());
            md5.update(pass.getBytes("US-ASCII"));
            md5.update("AOL Instant Messenger (SM)".getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException impossible) { return null; }

        return md5.digest();
    }

    /** The user's screenname. */
    private final String sn;

    /** The user's client version information. */
    private final ClientVersionInfo version;

    /** The user's locale. */
    private final Locale locale;

    /** The user's password, encrypted. */
    private final ByteBlock encryptedPass;

    /**
     * Generates an auth request command from the given incoming SNAC packet.
     *
     * @param packet an authorization request SNAC packet
     */
    protected AuthRequest(SnacPacket packet) {
        super(CMD_AUTH_REQ);

        DefensiveTools.checkNull(packet, "packet");

        TlvChain chain = ImmutableTlvChain.readChain(packet.getData());

        sn = chain.getString(TYPE_SN);
        encryptedPass = chain.hasTlv(TYPE_ENCPASS)
                ? chain.getLastTlv(TYPE_ENCPASS).getData() : null;

        version = ClientVersionInfo.readClientVersionInfo(chain);

        String language = chain.getString(TYPE_LANG);
        String country = chain.getString(TYPE_COUNTRY);
        if (language != null && country != null) {
            locale = new Locale(language, country);
        } else {
            locale = null;
        }
    }

    /**
     * Creates an outgoing authorization request command with the given
     * screenname, password, client version, and authorization key, and with the
     * JVM's current locale.
     * <br>
     * <br>
     * Calling this method is equivalent to calling {@link #AuthRequest(String,
     * String, ClientVersionInfo, Locale, ByteBlock) new AuthRequest(sn, pass,
     * version, Locale.getDefault(), key)}.
     *
     * @param sn the user's screenname
     * @param pass the user's password
     * @param version a client information block
     * @param key an authorization key block provided by the server in a
     *        {@link KeyResponse}
     */
    public AuthRequest(String sn, String pass, ClientVersionInfo version,
            ByteBlock key) {
        this(sn, pass, version, Locale.getDefault(), key);
    }

    /**
     * Creates an outgoing authorization request command with the given
     * screenname, password, client version, locale, and authorization key.
     *
     * @param sn the user's screenname
     * @param pass the user's password
     * @param version a client information block
     * @param locale the user's locale
     * @param key an authorization key block provided by the server in a
     *        {@link KeyResponse}
     */
    public AuthRequest(String sn, String pass, ClientVersionInfo version,
            Locale locale, ByteBlock key) {
        super(CMD_AUTH_REQ);

        this.sn = sn;
        this.version = version;
        this.locale = locale;
        this.encryptedPass = ByteBlock.wrap(encryptPassword(pass, key));
    }

    /**
     * Returns the screen name whose login is being attempted.
     *
     * @return the user's screen name
     */
    public final String getScreenname() { return sn; }

    /**
     * Returns the user's client information block.
     *
     * @return the user's client version information
     */
    public final ClientVersionInfo getVersionInfo() { return version; }

    /**
     * Returns the user's locale.
     *
     * @return the user's locale
     */
    public final Locale getLocale() { return locale; }

    /**
     * The raw encrypted password sent in this authorization request.
     *
     * @return the user's password, encrypted
     */
    public final ByteBlock getEncryptedPass() { return encryptedPass; }

    public void writeData(OutputStream out) throws IOException {
        if (sn != null) {
            Tlv.getStringInstance(TYPE_SN, sn).write(out);
        }
        if (encryptedPass != null) {
            new Tlv(TYPE_ENCPASS, encryptedPass).write(out);
        }

        // right here WinAIM sends an empty 0x004c TLV, but it causes our MD5
        // password hash to stop working :(

        // when the value of the TLV sent on this line is 0x0109, we are able to
        // set a buddy icon using SSI (SSI item type 0x14). when the value is
        // 0x0001, like AIM 3.5 sends, we cannot. well, we can store one on the
        // server, but no buddy icon actually shows up in buddies' IM windows.
        Tlv.getUShortInstance(0x0016, 0x0109).write(out);

        // write the version TLV's
        if (version != null) version.write(out);

        // winaim sends this, so we do too.
        Tlv.getUIntInstance(0x0014, 0x000000d2).write(out);

        if (locale != null) {
            String country = locale.getCountry();
            if (!country.equals("")) {
                Tlv.getStringInstance(TYPE_COUNTRY, country).write(out);
            }

            String language = locale.getLanguage();
            if (!language.equals("")) {
                Tlv.getStringInstance(TYPE_LANG, language).write(out);
            }
        }

        // this lets us use SSI for our buddy lists.
        new Tlv(0x004a, ByteBlock.wrap(new byte[] { 0x01 })).write(out);
    }

    public String toString() {
        return "AuthRequest: " +
                "sn='" + sn + "'" +
                ", version='" + version + "'" +
                ", locale=" + locale;
    }
}
