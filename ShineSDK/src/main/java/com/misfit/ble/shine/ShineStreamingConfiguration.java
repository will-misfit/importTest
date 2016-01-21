package com.misfit.ble.shine;

public class ShineStreamingConfiguration {
	public int mNumberOfMappedEventPackets = -1;
	public long mConnectionHeartbeatInterval = -1;
	
	public ShineStreamingConfiguration clone() {
		ShineStreamingConfiguration config = new ShineStreamingConfiguration();
		config.mNumberOfMappedEventPackets = this.mNumberOfMappedEventPackets;
		config.mConnectionHeartbeatInterval = this.mConnectionHeartbeatInterval;
		return config;
	}
}
