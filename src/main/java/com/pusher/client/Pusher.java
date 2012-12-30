package com.pusher.client;

import com.pusher.client.channel.Channel;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.impl.ChannelManager;
import com.pusher.client.channel.impl.InternalChannel;
import com.pusher.client.connection.Connection;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.impl.InternalConnection;
import com.pusher.client.util.Factory;

public class Pusher {

    private final InternalConnection connection;
    private final ChannelManager channelManager;
    
    public Pusher(String apiKey) {
	
	if(apiKey == null || apiKey.isEmpty()) {
	    throw new IllegalArgumentException("API Key cannot be null or empty");
	}
	
	this.connection = Factory.getConnection(apiKey);
	this.channelManager = Factory.getChannelManager(connection);
    }
    
    /* Connection methods */
    
    /**
     * Gets the underlying {@link Connection} object that is being used by this instance of {@linkplain Pusher}. 
     * @return The {@link Connection} object.
     */
    public Connection getConnection() {
	return connection;
    }
    
    /**
     * Connects to Pusher. Any {@link ConnectionEventListener}s that have already been registered using the 
     * {@link Connection#bind(ConnectionState, ConnectionEventListener)} method will receive connection events.
     */
    public void connect() {
	connect(null);
    }
    
    /**
     * Binds a {@link ConnectionEventListener} to the specified events and then connects to Pusher. This is equivalent to 
     * binding a {@link ConnectionEventListener} using the {@link Connection#bind(ConnectionState, ConnectionEventListener)} method
     * before connecting.
     * 
     * @param eventListener A {@link ConnectionEventListener} that will receive connection events. This can be null if you are not 
     * interested in receiving connection events, in which case you should call {@link #connect()} instead of this method.
     * @param connectionStates An optional list of {@link ConnectionState}s to bind your {@link ConnectionEventListener} to before
     * connecting to Pusher. If you do not specify any {@link ConnectionState}s then your {@link ConnectionEventListener} will be
     * bound to all connection events. This is equivalent to calling {@link #connect(ConnectionEventListener, ConnectionState...)}
     * with {@link ConnectionState#ALL}.
     * @throws IllegalArgumentException If the {@link ConnectionEventListener} is null and at least one connection state has
     * been specified.
     */
    public void connect(ConnectionEventListener eventListener, ConnectionState... connectionStates) {
	
	if(eventListener != null) {
	    if(connectionStates.length == 0) {
		connectionStates = new ConnectionState[] { ConnectionState.ALL };
	    }
		
	    for(ConnectionState state : connectionStates) {
		connection.bind(state, eventListener);
	    }
	} else {
	    if(connectionStates.length > 0) {
		throw new IllegalArgumentException("Cannot bind to connection states with a null connection event listener");
	    }
	}
	
	connection.connect();
    }
    
    /* Subscription methods */

    /**
     * Subscribes to a public {@link Channel}.
     * 
     * @param channelName The name of the {@link Channel} to subscribe to.
     * @return The {@link Channel} object representing your subscription.
     */
    public Channel subscribe(String channelName) {
	return subscribe(null);
    }
    
    /**
     * Binds a {@link ChannelEventListener} to the specified events and then subscribes to a public {@link Channel}.
     * 
     * @param channelName The name of the {@link Channel} to subscribe to.
     * @param listener A {@link ChannelEventListener} to receive events. This can be null if you don't want to bind a
     * listener at subscription time, in which case you should call {@link #subscribe(String)} instead of this method.
     * @param eventNames An optional list of event names to bind your {@link ChannelEventListener} to before subscribing.
     * @return The {@link Channel} object representing your subscription.
     * @throws IllegalArgumentException If any of the following are true:
     * <ul>
     * 	<li>The channel name is null.</li>
     *  <li>You are already subscribed to this channel.</li>
     *  <li>At least one of the specified event names is null.</li>
     *  <li>You have specified at least one event name and your {@link ChannelEventListener} is null.</li>
     * </ul>
     * @throws IllegalStateException If your {@link Connection} is not currently connected to Pusher.
     */
    public Channel subscribe(String channelName, ChannelEventListener listener, String... eventNames) {
	
	if(connection.getState() != ConnectionState.CONNECTED) {
	    throw new IllegalStateException("Cannot subscribe to public channel " + channelName + " while not connected");
	}
	
	InternalChannel channel = Factory.newPublicChannel(channelName);
	channelManager.subscribeTo(channel, listener, eventNames);
	
	return channel;
    }
    
    public void unsubscribe(String channelName) {
	
	if(connection.getState() != ConnectionState.CONNECTED) {
	    throw new IllegalStateException("Cannot unsubscribe from channel " + channelName + " while not connected");
	}
	
	channelManager.unsubscribeFrom(channelName);
    }
}