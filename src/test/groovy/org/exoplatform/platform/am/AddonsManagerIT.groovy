/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This file is part of eXo Platform - Add-ons Manager.
 *
 * eXo Platform - Add-ons Manager is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * eXo Platform - Add-ons Manager software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with eXo Platform - Add-ons Manager; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.platform.am

import groovy.util.slurpersupport.GPathResult
import org.exoplatform.platform.am.settings.PlatformSettings

/**
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
class AddonsManagerIT extends IntegrationTestsSpecification {

  final static Map<String, String[]> FOO_ADDON_42_CONTENT = [
      libraries: ["foo-addon-42.jar"],
      webapps  : ["foo-addon-42.war"]
  ]

  final static Map<String, String[]> OTHER_FILES_ADDON_42_CONTENT = [
      libraries  : ["other-files-addon-42.jar"],
      webapps    : ["other-files-addon-42.war"],
      othersFiles: ["conf/other-files-addon/configuration1.properties", "conf/other-files-addon/configuration2.properties"]
  ]

  final static Map<String, String[]> README_ADDON_42_CONTENT = [
      libraries: ["readme-addon-42.jar"],
      webapps  : ["readme-addon-42.war"]
  ]

  final static Map<String, String[]> FOO_ADDON_43_RC1_CONTENT = [
      libraries: ["foo-addon-43-RC1.jar"],
      webapps  : ["foo-addon-43-RC1.war"]
  ]

  final static Map<String, String[]> FOO_ADDON_43_SNAPSHOT_CONTENT = [
      libraries: ["foo-addon-43-SNAPSHOT.jar"],
      webapps  : ["foo-addon-43-SNAPSHOT.war"]
  ]

  def cleanup() {
    // After each test we remove the content of the add-ons directory to be safe
    getEnvironmentSettings().getAddonsDirectory().deleteDir()
  }

  def "Without any param the program must return an error"() {
    expect:
    AddonsManagerConstants.RETURN_CODE_INVALID_COMMAND_LINE_PARAMS == launchAddonsManager([""]).exitValue()
  }

  def "[AM_CLI_02] With --help param the program must display the help"() {
    expect:
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["--help"]).exitValue()
  }

  /**
   * list each Add-on of the Catalog which have at least 1 stable version
   * for each listed Add-on, list all the stable versions
   * for each listed Add-on, never list the development and unstable versions
   * don't list add-ons which contains only development or unstable versions
   */
  def "[AM_LIST_01] addon(.bat) list"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["list"]).exitValue()
  }

  /**
   * list each add-on of the Catalog (stable and development versions)
   * for each listed Add-on, list all the versions (stable and development)
   */
  def "[AM_LIST_02] addon(.bat) list --snapshots"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["list", "--snapshots"]).exitValue()
  }

  /**
   * List stable and unstable add-ons
   */
  def "[AM_LIST_03] addon(.bat) list --unstable"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["list", "--unstable"]).exitValue()
  }

  /**
   * list each add-on in the catalog at http://example.org/list.json
   */
  def "[AM_LIST_04] add-on.(sh|bat) list --catalog=http://example.org/list.json"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["list", "--catalog=${getWebServerRootUrl()}/catalog2.json"]).exitValue()
  }

  /**
   * same as list without arguments, but does not use the cached catalog
   */
  def "[AM_LIST_05] add-on.(sh|bat) list --no-cache"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["list", "--no-cache"]).exitValue()
  }

  /**
   * same as list without arguments,only using the local + cached catalogs
   */
  def "[AM_LIST_06] addon(.bat) list --offline"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["list", "--offline"]).exitValue()
  }

  /**
   * same as list without arguments, only using the local catalog
   */
  def "[AM_LIST_07] add-on.(sh|bat) list --offline --no-cache"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["list", "--no-cache", "--offline"]).exitValue()
  }

  /**
   * list each add-on installed locally (stable and development versions)
   */
  def "[AM_LIST_08] add-on.(sh|bat) list --installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["list", "--installed"]).exitValue()
  }

  /**
   * List installed stable add-ons for which a newer version is available based aether generic version order
   */
  def "[AM_LIST_09] addon(.bat) list --outdated"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon:40"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["list", "--outdated"]).exitValue()
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * List installed stable and unstable add-ons for which a newer version is available based aether generic version order
   */
  def "[AM_LIST_09a] addon(.bat) list --outdated --unstable"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon:40"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["list", "--outdated", "--unstable"]).exitValue()
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * List installed stable and snapshots add-ons for which a newer version is available based aether generic version order
   */
  def "[AM_LIST_09b] addon(.bat) list --outdated --snapshots"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon:40"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["list", "--outdated", "--snapshots"]).exitValue()
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * Unless --no-compat option is passed, the list is filtered to match the app server with supportedApplicationServers,
   * supportedDistributions, compatibility.
   */
  def "[AM_LIST_10] [AM_LIST_11] [AM_LIST_12] addon(.bat) list --no-compat"() {
    setup:
    ProcessResult process = launchAddonsManager(["list", "--no-compat"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == process.exitValue()
    // Verify the output
    process.stdoutText =~ "incompatible-foo-addon"
  }

  /**
   * if the foo-addon exists and has at least 1 released version : list all the informations about the most recent released version of foo-addon
   * if the foo-addon exists and has no released version (only snapshots) : must raise an error saying "The add-on foo-addon doesn't doesn't have a released version yet ! add snapshot option to use the snapshot version [KO]"
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the catalog, check your add-on name [KO]"
   */
  def "[AM_INF_01] addon(.bat) describe foo-addon"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["describe", "foo-addon"]).exitValue()
  }

  /**
   * if the foo-addon exists and has released version 42 : list all the informations the version42 of foo-addon
   * if the foo-addon exists and has no released version 42 : must raise an error saying "The add-on foo-addon doesn't have a released version 42 yet ! check the version you specify [KO]"
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the catalog, check your add-on name [KO]"
   */
  def "[AM_INF_02] addon(.bat) describe foo-addon:42"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["describe", "foo-addon:42"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the most recent stable version of foo-addon
   */
  def "[AM_INST_01] addon(.bat) install foo-addon - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["install", "foo-addon"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_42_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must not install anything
   */
  def "[AM_INST_01] addon(.bat) install foo-addon - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_ALREADY_INSTALLED == launchAddonsManager(
        ["install", "foo-addon"]).exitValue()
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "[AM_INST_01] addon(.bat) install foo-addon - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the most recent development version of the foo-addon
   */
  def "[AM_INST_02] addon(.bat) install foo-addon --snapshots - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["install", "foo-addon", "--snapshots"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_SNAPSHOT_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must not install anything
   * TODO : if the last stable version is more recent than the most recent development version, we must install the stable version
   */
  def "[AM_INST_02] addon(.bat) install foo-addon --snapshots - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon", "--snapshots"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_ALREADY_INSTALLED == launchAddonsManager(
        ["install", "foo-addon", "--snapshots"]).exitValue()
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "[AM_INST_02] addon(.bat) install foo-addon --snapshots - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon", "--snapshots"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the most recent unstable version of the foo-addon
   */
  def "addon(.bat) install foo-addon --unstable - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["install", "foo-addon", "--unstable"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_RC1_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must not install anything
   * TODO : if the last stable version is more recent than the most recent unstable version, we must install the stable version
   */
  def "addon(.bat) install foo-addon --unstable - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon", "--unstable"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_ALREADY_INSTALLED == launchAddonsManager(
        ["install", "foo-addon", "--unstable"]).exitValue()
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "addon(.bat) install foo-addon --unstable - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon", "--unstable"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the most recent stable version of the foo-addon
   */
  def "[AM_INST_03] addon(.bat) install foo-addon --force - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["install", "foo-addon", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_42_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must enforce to reinstall the foo-addon
   */
  def "[AM_INST_03] addon(.bat) install foo-addon --force - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["install", "foo-addon", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_42_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "[AM_INST_03] addon(.bat) install foo-addon --force - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon", "--force"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the most recent development version of the foo-addon
   */
  def "[AM_INST_04] addon(.bat) install foo-addon --snapshots --force - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon", "--snapshots", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_SNAPSHOT_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must enforce to reinstall the foo-addon with its more recent development version
   * TODO : if the last stable version is more recent than the most recent development version, we must install / reinstall the stable
   * version
   */
  def "[AM_INST_04] addon(.bat) install foo-addon --snapshots --force - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon", "--snapshots"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon", "--snapshots", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_SNAPSHOT_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "[AM_INST_04] addon(.bat) install foo-addon --snapshots --force - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon", "--snapshots", "--force"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the most recent unstable version of the foo-addon
   */
  def "addon(.bat) install foo-addon --unstable --force - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon", "--unstable", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_RC1_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must enforce to reinstall the foo-addon with its more recent development version
   * TODO : if the last stable version is more recent than the most recent unstable version, we must install / reinstall the stable
   * version
   */
  def "addon(.bat) install foo-addon --unstable --force - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon", "--unstable"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon", "--unstable", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_RC1_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "addon(.bat) install foo-addon --unstable --force - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon", "--unstable", "--force"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the version 42 of the foo-addon
   */
  def "[AM_INST_05] addon(.bat) install foo-addon:42 - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["install", "foo-addon:42"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_42_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must not install anything
   */
  def "[AM_INST_05] addon(.bat) install foo-addon:42 - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_ALREADY_INSTALLED == launchAddonsManager(
        ["install", "foo-addon:42"]).exitValue()
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "[AM_INST_05] addon(.bat) install foo-addon - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon:42"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the last 42 snapshot version available of the foo-addon
   */
  def "[AM_INST_06] addon(.bat) install foo-addon:43-SNAPSHOT --snapshots - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon:43-SNAPSHOT", "--snapshots"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_SNAPSHOT_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must not install anything
   */
  def "[AM_INST_06] addon(.bat) install foo-addon:43-SNAPSHOT --snapshots - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon:43-SNAPSHOT", "--snapshots"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_ALREADY_INSTALLED == launchAddonsManager(
        ["install", "foo-addon", "--snapshots"]).exitValue()
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "[AM_INST_06] addon(.bat) install foo-addon:43-SNAPSHOT --snapshots - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon:43-SNAPSHOT", "--snapshots"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the 43-RC1 version of the foo-addon
   */
  def "addon(.bat) install foo-addon:43-RC1 --unstable - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon:43-RC1", "--unstable"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_RC1_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must not install anything
   */
  def "addon(.bat) install foo-addon:43-RC1 --unstable - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon:43-RC1", "--unstable"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_ALREADY_INSTALLED == launchAddonsManager(
        ["install", "foo-addon", "--unstable"]).exitValue()
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "addon(.bat) install foo-addon:43-RC1 --unstable - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon:43-RC1", "--unstable"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the version 42 of the foo-addon
   */
  def "[AM_INST_07] addon(.bat) install foo-addon:42 --force - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["install", "foo-addon:42", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_42_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must enforce to reinstall the version 42 of the foo-addon
   */
  def "[AM_INST_07] addon(.bat) install foo-addon:42 --force - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["install", "foo-addon:42", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_42_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "[AM_INST_07] addon(.bat) install foo-addon:42 --force - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon:42", "--force"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the most recent 43-SNAPSHOT development version of the foo-addon
   */
  def "[AM_INST_08] addon(.bat) install foo-addon:43-SNAPSHOT --snapshots --force - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon:43-SNAPSHOT", "--snapshots", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_SNAPSHOT_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must enforce to reinstall the foo-addon with its more recent 43-SNAPSHOT development
   * version
   */
  def "[AM_INST_08] addon(.bat) install foo-addon:43-SNAPSHOT --snapshots --force - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon", "--snapshots"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon:43-SNAPSHOT", "--snapshots", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_SNAPSHOT_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "[AM_INST_08] addon(.bat) install foo-addon:43-SNAPSHOT --snapshots --force - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon:43-SNAPSHOT", "--snapshots", "--force"]).exitValue()
  }

  /**
   * if foo-addon not already installed : must install the 43-RC1 version of the foo-addon
   */
  def "addon(.bat) install foo-addon:43-RC1 --unstable --force - not yet installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon:43-RC1", "--unstable", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_RC1_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon is already installed : must enforce to reinstall the foo-addon with its 43-RC1 version
   */
  def "addon(.bat) install foo-addon:43-RC1 --unstable --force - already installed"() {
    setup:
    // Install it first
    launchAddonsManager(["install", "foo-addon", "--unstable"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon:43-RC1", "--unstable", "--force"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_43_RC1_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
  }

  /**
   * if foo-addon doesn't exists in the catalog : must raise an error saying "The add-on foo-addon doesn't exists in the remote
   * catalog, check your add-on name [KO]"
   */
  def "addon(.bat) install foo-addon:43-RC1 --unstable --force - not found"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND == launchAddonsManager(
        ["install", "unknown-foo-addon:43-RC1", "--unstable", "--force"]).exitValue()
  }

  /**
   * The add-ons manager does a compatibility check using the compatibility values prior to install an add-on. If the add-on is
   * not compatible, the installation interrupts with an error : "The add-on foo-addon:version is not compatible with your
   * version of eXo Platform. Use --no-compat to ignore this compatibility check and install anyway.
   */
  def "[AM_INST_09] The add-ons manager does a compatibility check using the compatibility values prior to install an add-on."() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_INCOMPATIBILITY_ERROR == launchAddonsManager(
        ["install", "incompatible-foo-addon:42"]).exitValue()
  }

  /**
   * installs foo-addon version 1.2 ignoring the compatiblity check
   */
  def "[AM_INST_10] addon(.bat) install foo-addon --no-compat"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "incompatible-foo-addon:42", "--no-compat"]).exitValue()
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "incompatible-foo-addon"])
  }

  /**
   * If installation requires to install an existing file, the default behaviour is to abort the installation with an error :
   * File XYZ already exists. Installation aborted. Use --conflict=skip|overwrite.
   * --conflict=skip will skip the conflicted files and log a warning for each one : File XYZ already exists. Skipped.
   * --conflict=overwrite will overwrite the conflicted files by the one contained in the add-on and log a warning for each one
   * : File XYZ already exists. Overwritten.
   */
  def "[AM_INST_11] addon(.bat) install foo-addon --conflict=skip"() {
    setup:
    // Let's create a file existing in the addon
    new File(getPlatformSettings().librariesDirectory, "foo-addon-42.jar") << "TEST"
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon:42", "--conflict=skip"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_42_CONTENT)
    // It shouldn't have been touched
    new File(getPlatformSettings().librariesDirectory, "foo-addon-42.jar").text == "TEST"
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
    // Manually remove or additional file
    new File(getPlatformSettings().librariesDirectory, "foo-addon-42.jar").delete()
  }

  /**
   * If installation requires to install an existing file, the default behaviour is to abort the installation with an error :
   * File XYZ already exists. Installation aborted. Use --conflict=skip|overwrite.
   * --conflict=skip will skip the conflicted files and log a warning for each one : File XYZ already exists. Skipped.
   * --conflict=overwrite will overwrite the conflicted files by the one contained in the add-on and log a warning for each one
   * : File XYZ already exists. Overwritten.
   */
  def "[AM_INST_11] addon(.bat) install foo-addon --conflict=overwrite"() {
    setup:
    // Let's create a file existing in the addon
    new File(getPlatformSettings().librariesDirectory, "foo-addon-42.jar") << "TEST"
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "foo-addon:42", "--conflict=overwrite"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_42_CONTENT)
    // It should have been replaced
    new File(getPlatformSettings().librariesDirectory, "foo-addon-42.jar").text != "TEST"
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "foo-addon"])
    // Manually remove or additional file
    new File(getPlatformSettings().librariesDirectory, "foo-addon-42.jar").delete()
  }

  /**
   * At the end of a successful install command, the README of the add-on is displayed in the console if present.
   */
  def "[AM_INST_12] At the end of a successful install command, the README of the add-on is displayed in the console if present."() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "readme-addon:42"], ['', '', '', '', '', '', '', '', '', '', '']).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(README_ADDON_42_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "readme-addon"])
  }

  /**
   * Other files and folders located at the root of the add-on archive are copied as-is under $PLATFORM_HOME
   */
  def "[AM_STRUCT_04] addon(.bat) install other-files-addon"() {
    expect:
    // Install it
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["install", "other-files-addon:42"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(OTHER_FILES_ADDON_42_CONTENT)
    // Uninstall it
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["uninstall", "other-files-addon"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentNotPresent(OTHER_FILES_ADDON_42_CONTENT)
  }

  /**
   * if foo-addon not already installed : must raise an error saying "The add-on foo-addon was not installed [KO]"
   */
  def "[AM_UNINST_01] addon(.bat) uninstall foo-addon - not already installed"() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_ADDON_NOT_INSTALLED == launchAddonsManager(
        ["uninstall", "foo-addon"]).exitValue()
  }

  /**
   * if foo-addon is already installed : must uninstall the add-on whatever the installed version is stable or development
   */
  def "[AM_UNINST_01] addon(.bat) uninstall foo-addon - already installed"() {
    setup:
    launchAddonsManager(["install", "foo-addon:42"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["uninstall", "foo-addon"]).exitValue()
    // Verify that the add-on is not installed
    verifyAddonContentNotPresent(FOO_ADDON_42_CONTENT)
  }

  /**
   * At uninstall, files that were already existing are not removed unless they were overwritten with (--conflict=overwrite in
   * which case, the previous version of the file is restored and the following warning message is logged : File XYZ has been
   * restored
   */
  def "[AM_UNINST_02] Removal of add-ons installed with --conflict=overwrite"() {
    setup:
    // Let's create a file existing in the addon
    new File(getPlatformSettings().librariesDirectory, "foo-addon-42.jar") << "TEST"
    launchAddonsManager(["install", "foo-addon:42", "--conflict=overwrite"])
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["uninstall", "foo-addon"]).exitValue()
    // Verify that the add-on is remove installed
    !new File(getPlatformSettings().webappsDirectory, "foo-addon-42.war").exists()
    // But the replaced file should be restored
    new File(getPlatformSettings().librariesDirectory, "foo-addon-42.jar").exists()
    new File(getPlatformSettings().librariesDirectory, "foo-addon-42.jar").text == "TEST"
    cleanup:
    // Manually remove the additional file
    new File(getPlatformSettings().librariesDirectory, "foo-addon-42.jar").delete()
  }

  /**
   * [LICENSE_01] Download and display license if mustAcceptLicenseTerms=true
   * [LICENSE_03] [LICENSE_04] interactive validation of license
   */
  def "[LICENSE_01] [LICENSE_03] [LICENSE_04] Download and display license if mustAcceptLicenseTerms=true. The user refuses it."() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_LICENSE_NOT_ACCEPTED == launchAddonsManager(
        ["install", "license-addon:42"],
        ['no\n']).exitValue()
    // Verify that the add-on isn't installed
    verifyAddonContentNotPresent(FOO_ADDON_42_CONTENT)
  }

  /**
   * [LICENSE_01] Download and display license if mustAcceptLicenseTerms=true
   * [LICENSE_03] [LICENSE_04] interactive validation of license
   */
  def "[LICENSE_01] [LICENSE_03] [LICENSE_04] Download and display license if mustAcceptLicenseTerms=true. The user accepts it."() {
    expect:
    // Verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(
        ["install", "license-addon:42"],
        ['yes\n']).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_42_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "license-addon"])
  }

  /**
   * [LICENSE_05] Don't prompt to validate a license already accepted
   */
  def "[LICENSE_05] Don't prompt to validate a license already accepted."() {
    expect:
    // Install it a first time
    launchAddonsManager(["install", "license-addon:42"], ['yes\n'])
    // Remove it
    launchAddonsManager(["uninstall", "license-addon"])
    // Reinstall it and verify return code
    AddonsManagerConstants.RETURN_CODE_OK == launchAddonsManager(["install", "license-addon:42"]).exitValue()
    // Verify that the add-on is correctly installed
    verifyAddonContentPresent(FOO_ADDON_42_CONTENT)
    cleanup:
    // Uninstall it
    launchAddonsManager(["uninstall", "license-addon"])
  }

  /**
   * Helper method to check that an add-on is correctly installed
   * @param addonContent a Map describing the add-on content. It may have 3 keys and for each a list of paths in the archive.
   * @{code libraries} gives le list of libraries (*.jar) in the archive. @{code webapps} gives le list of webapps (*.war)
   * in the archive. @{code othersFiles} gives le list of others files.
   */
  void verifyAddonContentPresent(Map<String, String[]> addonContent) {
    if (addonContent.libraries) {
      addonContent.libraries.each { library ->
        assert new File(getPlatformSettings().librariesDirectory, library).exists()
      }
    }
    if (addonContent.webapps) {
      addonContent.webapps.each { webapp ->
        assert new File(getPlatformSettings().webappsDirectory, webapp).exists()
        if (PlatformSettings.AppServerType.JBOSS == getPlatformSettings().appServerType) {
          // Verify the application.xml
          GPathResult applicationXmlContent = new XmlSlurper(false, false).parseText(
              new File(getPlatformSettings().webappsDirectory, "META-INF/application.xml").text)
          assert applicationXmlContent.depthFirst().findAll {
            (it.name() == 'module') &&
                (it.'web'.'context-root'.text() == webapp.substring(0, webapp.size() - 4)) &&
                (it.'web'.'web-uri'.text() == webapp)
          }.size() == 1
        }
      }
    }
    if (addonContent.othersFiles) {
      addonContent.othersFiles.each { otherFile ->
        assert new File(getPlatformSettings().homeDirectory, otherFile).exists()
      }
    }
  }

  /**
   * Helper method to check that an add-on is not installed
   * @param addonContent a Map describing the add-on content. It may have 3 keys and for each a list of paths in the archive.
   * @{code libraries} gives le list of libraries (*.jar) in the archive. @{code webapps} gives le list of webapps (*.war)
   * in the archive. @{code othersFiles} gives le list of others files.
   */
  void verifyAddonContentNotPresent(Map<String, String[]> addonContent) {
    if (addonContent.libraries) {
      addonContent.libraries.each { library ->
        assert !new File(getPlatformSettings().librariesDirectory, library).exists()
      }
    }
    if (addonContent.webapps) {
      addonContent.webapps.each { webapp ->
        assert !new File(getPlatformSettings().webappsDirectory, webapp).exists()
        if (PlatformSettings.AppServerType.JBOSS == getPlatformSettings().appServerType) {
          // Verify the application.xml
          GPathResult applicationXmlContent = new XmlSlurper(false, false).parseText(
              new File(getPlatformSettings().webappsDirectory, "META-INF/application.xml").text)
          assert applicationXmlContent.depthFirst().findAll {
            (it.name() == 'module') &&
                ((it.'web'.'context-root'.text() == webapp.substring(0, webapp.size() - 4)) ||
                    (it.'web'.'web-uri'.text() == webapp))
          }.size() == 0
        }

      }
    }
    if (addonContent.othersFiles) {
      addonContent.othersFiles.each { otherFile ->
        assert !new File(getPlatformSettings().homeDirectory, otherFile).exists()
      }
    }
  }

}