//*********************************************************************************
// Added by Dustin Schultz for FURTHeR 02/14/11
//*********************************************************************************
// this file contains a list of all files that need to be loaded dynamically for this i2b2 Cell
// every file in this list will be loaded after the cell's Init function is called
{
	files: [
		"i2b2_msgs.js"
	],
	config: {
		// additional configuration variables that are set by the system
		name: "FURTHeR Cell",
		description: "The FURTHeR cell is responsible for communicating with FURTHeR",
		icons: {
			size32x32: "PM_icon_32x32.gif"
		},
		category: ["core","cell"]
	}
}