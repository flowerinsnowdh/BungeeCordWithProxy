package net.md_5.bungee.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class HttpInitializerWithProxy extends ChannelInitializer<Channel>
{

    private final Callback<String> callback;
    private final boolean ssl;
    private final String host;
    private final int port;

    @Override
    protected void initChannel(Channel ch) throws Exception
    {
        if (System.getProperty("bungeecord.socksProxyHost") != null) {
            String socksProxyHost = System.getProperty("bungeecord.socksProxyHost");
            String socksProxyPort = System.getProperty("bungeecord.socksProxyPort");
            String socksProxyUserName = System.getProperty("bungeecord.socksProxyUserName");
            String socksProxyPassword = System.getProperty("bungeecord.socksProxyPassword");
            try {
                ch.pipeline().addLast(
                        new Socks5ProxyHandler(
                                new InetSocketAddress(socksProxyHost, Integer.parseInt(socksProxyPort)),
                                socksProxyUserName, socksProxyPassword
                        )
                );
            } catch (NumberFormatException ignored) {
            }
        } else if (System.getProperty("bungeecord.httpProxyHost") != null) {
            String httpProxyHost = System.getProperty("bungeecord.httpProxyHost");
            String httpProxyPort = System.getProperty("bungeecord.httpProxyPort");
            String httpProxyUserName = System.getProperty("bungeecord.httpProxyUserName");
            String httpProxyPassword = System.getProperty("bungeecord.httpProxyPassword");
            try {
                ch.pipeline().addLast(
                        new HttpProxyHandler(
                                new InetSocketAddress(httpProxyHost, Integer.parseInt(httpProxyPort)),
                                httpProxyUserName, httpProxyPassword
                        )
                );
            } catch (NumberFormatException ignored) {
            }
        }
        ch.pipeline().addLast( "timeout", new ReadTimeoutHandler( HttpClient.TIMEOUT, TimeUnit.MILLISECONDS ) );
        if ( ssl )
        {
            SSLEngine engine = SslContextBuilder.forClient().build().newEngine( ch.alloc(), host, port );

            ch.pipeline().addLast( "ssl", new SslHandler( engine ) );
        }
        ch.pipeline().addLast( "http", new HttpClientCodec() );
        ch.pipeline().addLast( "handler", new HttpHandler( callback ) );
    }
}
