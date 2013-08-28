/**
 * @projectDescription Help file viewer
 * @inherits i2b2
 * @namespace i2b2
 * @author Nick Benik, Griffin Weber MD PhD, N. Dustin Schultz
 * @version 1.3
 *          ----------------------------------------------------------------------------------------
 *          updated 9-15-08: RC4 launch [Nick Benik]
 */
console.group('Load & Execute component file: hive > helpviewer');
console.time('execute time');

i2b2.hive.HelpViewer = {
	panels : new Array(),
	show : function(idPrefix, resource) {
		if (!i2b2.hive.HelpViewer.panels[idPrefix]) {
			var elementPanelId = idPrefix + "-viewer-panel";
			var elementBodyId = idPrefix + "-viewer-body";
			// show non-modal dialog with help documentation
			var panel = new YAHOO.widget.Panel(elementPanelId, {
				draggable : true,
				zindex : 10000,
				width : "650px",
				height : "485px",
				autofillheight : "body",
				constraintoviewport : true,
				context : [ "showbtn", "tl", "bl" ]
			});
			$(elementPanelId).show();
			panel.render(document.body);
			panel.center();
			panel.show();
			i2b2.hive.HelpViewer.panels[idPrefix] = panel;

			// resizer object and event handlers
			i2b2.hive.HelpViewer.resizer = new YAHOO.util.Resize(
					elementPanelId, {
						handles : [ 'br' ],
						autoRatio : false,
						minWidth : 300,
						minHeight : 200,
						status : false
					});

			i2b2.hive.HelpViewer.resizer.on('resize', function(args) {
				var panelHeight = args.height;
				this.cfg.setProperty("height", panelHeight + "px");
			}, i2b2.hive.HelpViewer.panels[idPrefix], true);

			i2b2.hive.HelpViewer.resizer.on('startResize', function(args) {
				if (this.cfg.getProperty("constraintoviewport")) {
					var D = YAHOO.util.Dom;
					var clientRegion = D.getClientRegion();
					var elRegion = D.getRegion(this.element);
					resize.set("maxWidth", clientRegion.right - elRegion.left
							- YAHOO.widget.Overlay.VIEWPORT_OFFSET);
					resize.set("maxHeight", clientRegion.bottom - elRegion.top
							- YAHOO.widget.Overlay.VIEWPORT_OFFSET);
				} else {
					resize.set("maxWidth", null);
					resize.set("maxHeight", null);
				}
			}, i2b2.hive.HelpViewer.panels[idPrefix], true);
		} else {
			i2b2.hive.HelpViewer.panels[idPrefix].show();
		}

		if (Object.isFunction(resource)) {
			resource(elementBodyId);
		} else {
			// load the help page
			new Ajax.Updater(elementBodyId, resource, {
				method : 'get',
				parameters : {
					cell : 'CORE',
					page : 'ROOT'
				}
			});
		}
	}
};

i2b2.hive.DataSourceInfoViewer = {
		show : function(idPrefix) {
			i2b2.hive.HelpViewer.show(idPrefix, function(elementBodyId) {
				if (!i2b2.hive.DataSourceInfoViewer.content) {
					i2b2.FUR.ajax.GetDatasourceInfo("FUR:ONT:Help", {
						resourceName : "general-ds-metadata.html"
					}, function(results) {
						i2b2.hive.DataSourceInfoViewer.content = results.msgResponse;
						$(elementBodyId).update(i2b2.hive.DataSourceInfoViewer.content);
					});
				}
			});
		}
};


console.timeEnd('execute time');
console.groupEnd();
