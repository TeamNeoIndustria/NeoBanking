package xyz.neonetwork.neobanking.client;

public class PDAPacketParser {
	// Server -> Client
	// Balance - int
	// History - List<IRSTransaction>
	// Leaderboard - List<IRSLeaderboardEntry>
	// Pending - List<IRSTransaction>

	// Client -> Server
	// Request Money - IRSTransaction
	// Send Money - IRSTransaction
	// Accept/Decline Request - IRSSimpleTransaction ?Maybe IRSTransaction?

}
