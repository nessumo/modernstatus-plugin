package org.jenkinsci.plugins.modernstatus;

import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.util.ColorPalette;
import hudson.util.PluginServletFilter;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
* Original
* @author Kohsuke Kawaguchi
* Removed logging and a few other things for my needs
* @author Oliver Vinn
*/
public class PluginImpl extends Plugin {

  transient PluginWrapper wrapper;

  @Override
  public void start() throws Exception {
    super.start();
    load();
    PluginServletFilter.addFilter(new ModernStatusFilter());
    try {
        wrapper = null;
        Field wrapperField = Plugin.class.getDeclaredField("wrapper");
        wrapperField.setAccessible(true);
        wrapper = (PluginWrapper) wrapperField.get(this);
    } catch (Exception e) {}
    try {
      Field colorValue = Color.class.getDeclaredField("value");
      colorValue.setAccessible(true);
      colorValue.setInt(ColorPalette.BLUE, new Color(124, 208, 25).getRGB());
    } catch (Exception e) {}
  }

  @Override
  public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    rsp.setHeader("Cache-Control", "public, s-maxage=86400");
    if (wrapper == null) {
      super.doDynamic(req, rsp);
      return;
    }

    String path = req.getRestOfPath();
    if (path.length() == 0) {
      path = "/";
    }
    if (path.indexOf("..") != -1 || path.length() < 1) {
      // don't serve anything other than files in the sub directory.
      rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // use serveLocalizedFile to support automatic locale selection
    rsp.serveLocalizedFile(req, new URL(wrapper.baseResourceURL, '.' + path), 86400000);
  }
}
