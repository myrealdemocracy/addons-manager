/**
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.platform.addon

import static org.fusesource.jansi.Ansi.ansi

/**
 * Command line utility to manage Platform addons.
 */

try {

// Initialize logging system
  Logging.initialize()
  def ManagerSettings managerSettings = new ManagerSettings()
// And display header
  Logging.displayHeader(managerSettings)
// Parse command line parameters and fill settings with user inputs
  managerSettings.cliArgs = CLI.initialize(args, new ManagerCLIArgs())
  if (managerSettings.cliArgs == null) {
    // Something went wrong, bye
    Logging.dispose()
    System.exit CLI.RETURN_CODE_KO
  } else if (managerSettings.cliArgs.action == ManagerCLIArgs.Action.HELP) {
    // Just asking for help
    Logging.dispose()
    System.exit CLI.RETURN_CODE_OK
  }

  // Platform settings initialization
  if (!System.getProperty("product.home")) {
    Logging.displayMsgError('error: Erroneous setup, system property product.home not defined.')
    System.exit CLI.RETURN_CODE_KO
  }
  PlatformSettings platformSettings = new PlatformSettings(new File(System.getProperty("product.home")))
  managerSettings.platformSettings = platformSettings
  if (!managerSettings.validate() || !platformSettings.validate()) {
    Logging.dispose()
    System.exit CLI.RETURN_CODE_KO
  }

  def List<Addon> addons = new ArrayList<Addon>()
  // Load add-ons list when listing them or installing one
  switch (managerSettings.cliArgs.action) {
    case [ManagerCLIArgs.Action.LIST, ManagerCLIArgs.Action.INSTALL]:
      // Let's load the list of available add-ons
      def catalog
      // Load the optional local list
      if (managerSettings.localAddonsCatalogFile.exists()) {
        Logging.logWithStatus("Reading local add-ons list...") {
          catalog = managerSettings.localAddonsCatalog
        }
        Logging.logWithStatus("Loading add-ons...") {
          addons.addAll(Addon.parseJSONAddonsList(catalog, managerSettings))
        }
      } else {
        Logging.displayMsgVerbose("No local catalog to load")
      }
      // Load the central list
      Logging.logWithStatus("Downloading central add-ons list...") {
        catalog = managerSettings.centralCatalog
      }
      Logging.logWithStatus("Loading add-ons...") {
        addons.addAll(Addon.parseJSONAddonsList(catalog, managerSettings))
      }
  }

  //
  switch (managerSettings.cliArgs.action) {
    case ManagerCLIArgs.Action.LIST:
      println ansi().render("\n@|bold Available add-ons:|@\n")
      addons.findAll { it.isStable() || managerSettings.cliArgs.snapshots }.groupBy { it.id }.each {
        Addon anAddon = it.value.first()
        printf(ansi().render("+ @|bold,yellow %-${addons.id*.size().max()}s|@ : @|bold %s|@, %s\n").toString(), anAddon.id,
               anAddon.name, anAddon.description)
        printf(ansi().render("     Available Version(s) : @|bold,yellow %-${addons.version*.size().max()}s|@ \n\n").toString(),
               ansi().render(it.value.collect { "@|yellow ${it.version}|@" }.join(', ')))
      }
      println ansi().render("""
  To install an add-on:
    ${CLI.getScriptName()} --install @|yellow addon|@
  """).toString()
      break
    case ManagerCLIArgs.Action.INSTALL:
      def addon
      if (managerSettings.cliArgs.addonVersion == null) {
        // Let's find the first add-on with the given id (including or not snapshots depending of the option)
        addon = addons.find {
          (it.isStable() || managerSettings.cliArgs.snapshots) && managerSettings.cliArgs.addonId.equals(it.id)
        }
        if (addon == null) {
          Logging.displayMsgError("No add-on with identifier ${managerSettings.cliArgs.addonId} found")
          Logging.dispose()
          System.exit CLI.RETURN_CODE_KO
        }
      } else {
        // Let's find the add-on with the given id and version
        addon = addons.find {
          managerSettings.cliArgs.addonId.equals(it.id) && managerSettings.cliArgs.addonVersion.equalsIgnoreCase(it.version)
        }
        if (addon == null) {
          Logging.displayMsgError(
              "No add-on with identifier ${managerSettings.cliArgs.addonId} and version ${managerSettings.cliArgs.addonVersion} found")
          Logging.dispose()
          System.exit CLI.RETURN_CODE_KO
        }
      }
      addon.install()
      break
    case ManagerCLIArgs.Action.UNINSTALL:
      def statusFile = Addon.getAddonStatusFile(platformSettings.addonsDirectory, managerSettings.cliArgs.addonId)
      if (statusFile.exists()) {
        def addon
        Logging.logWithStatus("Loading add-on details...") {
          addon = Addon.parseJSONAddon(statusFile.text, managerSettings);
        }
        addon.uninstall()
      } else {
        Logging.logWithStatusKO("Add-on not installed. Exiting.")
        Logging.dispose()
        System.exit CLI.RETURN_CODE_KO
      }
      break
    default:
      Logging.displayMsgError("Unsupported operation.")
      Logging.dispose()
      System.exit CLI.RETURN_CODE_KO
  }
} catch (Exception e) {
  Logging.displayException(e)
  System.exit CLI.RETURN_CODE_KO
} finally {
  Logging.dispose()
}

System.exit CLI.RETURN_CODE_OK