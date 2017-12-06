package com.soul.app.socketchat;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.soul.app.socketchat.*;
import com.soul.app.utils.Lg;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.BasicNameValuePair;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


public class WebSocketClient {
    private static final String TAG = "WebSocketClient";
    public static int i = 0;
    private static TrustManager[] sTrustManagers;
    private final Object mSendLock = new Object();
    private URI mURI;
    private Listener mListener;
    private Socket mSocket;
    private Thread mThread;
    private Handler mHandler;
    private List<BasicNameValuePair> mExtraHeaders;
    private com.soul.app.socketchat.HybiParser mParser;
    private boolean mConnected;

    public WebSocketClient(URI uri, Listener listener, List<BasicNameValuePair> extraHeaders) {
        mURI = uri;
        mListener = listener;
        mExtraHeaders = extraHeaders;
        mConnected = false;
        mParser = new com.soul.app.socketchat.HybiParser(this);

        HandlerThread mHandlerThread = new HandlerThread("websocket-thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public static void setTrustManagers(TrustManager[] tm) {
        sTrustManagers = tm;
    }

    public Listener getListener() {
        return mListener;
    }

    public void connect() {
        if (mThread != null && mThread.isAlive()) {
            return;
        }

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int port = (mURI.getPort() != -1) ? mURI.getPort() : ((mURI.getScheme().equals("wss") || mURI.getScheme().equals(
                            "https")) ? 5000 : 80);

                    String path = TextUtils.isEmpty(mURI.getPath()) ? "/" : mURI.getPath();
                    if (!TextUtils.isEmpty(mURI.getQuery())) {
                        path += "?" + mURI.getQuery();
                    }

                    String originScheme = mURI.getScheme().equals("wss") ? "https" : "http";
                    URI origin = new URI(originScheme, "//" + mURI.getHost(), null);

                    SocketFactory factory = (mURI.getScheme().equals("wss") || mURI.getScheme().equals("https")) ? getSSLSocketFactory()
                            : SocketFactory.getDefault();
                    mSocket = factory.createSocket(mURI.getHost(), port);
                    mSocket.setKeepAlive(true);
                    // mSocket.setSoTimeout(100000);
                    PrintWriter out = new PrintWriter(mSocket.getOutputStream());
                    String secretKey = createSecret();
                    out.print("GET " + path + " HTTP/1.1\r\n");
                    out.print("Upgrade: websocket\r\n");
                    out.print("Connection: Upgrade\r\n");
                    out.print("Host: " + mURI.getHost() + "\r\n");
                    out.print("Origin: " + origin.toString() + "\r\n");
                    out.print("Sec-WebSocket-Key: " + secretKey + "\r\n");
                    out.print("Sec-WebSocket-Version: 13\r\n");
                    if (mExtraHeaders != null) {
                        for (NameValuePair pair : mExtraHeaders) {
                            out.print(String.format("%s: %s\r\n", pair.getName(), pair.getValue()));
                        }
                    }
                    out.print("\r\n");
                    out.flush();

                    com.soul.app.socketchat.HybiParser.HappyDataInputStream stream = new com.soul.app.socketchat.HybiParser.HappyDataInputStream(mSocket.getInputStream());

                    // Read HTTP response status line.
                    StatusLine statusLine = parseStatusLine(readLine(stream));
                    if (statusLine == null) {
                        throw new HttpException("Received no reply from server.");
                    } else if (statusLine.getStatusCode() != HttpStatus.SC_SWITCHING_PROTOCOLS) {
                        throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                    }

                    // Read HTTP response headers.
                    String line;
                    while (!TextUtils.isEmpty(line = readLine(stream))) {
                        /*
                         * Header header = parseHeader(line); if
						 * (header.getName().equals("Sec-WebSocket-Accept")) {
						 * String expected = expectedKey(secretKey); if
						 * (expected == null) { throw new
						 * Exception("SHA-1 algorithm not found"); } else if
						 * (!expected.equals(header.getValue())) { throw new
						 * Exception( "Invalid Sec-WebSocket-Accept, expected: "
						 * + expected + ", got: " + header.getValue()); } }
						 */

                        Lg.e("TAG", "result :: " + line);
                    }

                    mListener.onConnect();

                    mConnected = true;

                    // Now decode websocket frames.
                    mParser.start(stream);

                } catch (EOFException ex) {
                    Lg.e(TAG, "WebSocket EOF!" + ex);
                    mListener.onDisconnect(0, "EOF");
                    mConnected = false;

                } catch (SSLException ex) {
                    // Connection reset by peer
                    Lg.e(TAG, "Websocket SSL error!" + ex);
                    mListener.onDisconnect(0, "SSL");
                    mConnected = false;

                } catch (Exception ex) {
                    mListener.onError(ex);
                }
            }
        });
        mThread.start();
    }

    public void disconnect() {
        if (mSocket != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSocket != null) {
                        try {
                            try {
                                mSocket.shutdownInput();

                                mSocket.shutdownOutput();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            mSocket.close();
                        } catch (IOException ex) {
                            Lg.e(TAG, "Error while disconnecting" + ex);
                            mListener.onError(ex);
                        }
                        mSocket = null;
                    }
                    mConnected = false;
                }
            });
        }
    }

    /**
     * this method is used to send the data from local to server
     *
     * @param data
     */
    public void send(final String data) {
        Log.d("Send Data", "" + data);

        sendFrame(mParser.frame(data));
    }

    public void send(byte[] data) {
        sendFrame(mParser.frame(data));
    }

    public boolean isConnected() {
        return mSocket != null && !(mSocket.isClosed() && !mSocket.isConnected());
//         return mConnected;
    }

    private StatusLine parseStatusLine(String line) {
        if (TextUtils.isEmpty(line)) {
            return null;
        }
        return BasicLineParser.parseStatusLine(line, new BasicLineParser());
    }

    private Header parseHeader(String line) {
        return BasicLineParser.parseHeader(line, new BasicLineParser());
    }

    // Can't use BufferedReader because it buffers past the HTTP data.
    private String readLine(com.soul.app.socketchat.HybiParser.HappyDataInputStream reader) throws IOException {
        int readChar = reader.read();
        if (readChar == -1) {
            return null;
        }
        StringBuilder string = new StringBuilder("");
        while (readChar != '\n') {
            if (readChar != '\r') {
                string.append((char) readChar);
            }

            readChar = reader.read();
            if (readChar == -1) {
                return null;
            }
        }
        return string.toString();
    }

    private String expectedKey(String secret) {
        // concatenate, SHA1-hash, base64-encode
        try {
            final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            final String secretGUID = secret + GUID;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(secretGUID.getBytes());
            return Base64.encodeToString(digest, Base64.DEFAULT).trim();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String createSecret() {
        byte[] nonce = new byte[16];
        for (int i = 0; i < 16; i++) {
            nonce[i] = (byte) (Math.random() * 256);
        }
        return Base64.encodeToString(nonce, Base64.DEFAULT).trim();
    }

    void sendFrame(final byte[] frame) {
        String aa = frame.toString();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mSendLock) {
                        OutputStream outputStream = mSocket.getOutputStream();
                        outputStream.write(frame);
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    mListener.onError(e);

                }
            }
        });
    }

    private SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, sTrustManagers, null);
        return context.getSocketFactory();
    }

    public interface Listener {
        void onConnect();

        void onMessage(String message);

        void onMessage(byte[] data);

        void onDisconnect(int code, String reason);

        void onError(Exception error);
    }
}