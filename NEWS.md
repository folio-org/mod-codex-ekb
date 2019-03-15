## 1.2.0 2019-03-15
 * [MODCXEKB-72] - Define normalized Package fields mapping
 * [MODCXEKB-74] - Define mormaized title-package fields
 * [MODCXEKB-79] - Implement GET Package by ID
 * [MODCXEKB-80] - Implement Get Package Collection
 * [MODCXEKB-81] - Implement Get Package Sources
 * [MODCXEKB-82] - Revise CQL Parser for RMAPI to handle Package Queries
 * [MODCXEKB-83] - Revise RMAPI To Codex to Handle Packages
 * [MODCXEKB-85] - Extract RMAPIService to a library module

## 1.1.0 2018-12-04
 * [MODCXEKB-64] - Update mod-codex-ekb to support Boolean/Exact Phrase/Wildcard/Nested Searches
 * [MODCXEKB-67] - Upgrade mod-codex-ekb to RAML 1.0
 * [MODCXEKB-68] - Return Subjects in mod-codex-ekb response
 * [MODCXEKB-69] - Support search by subject


## 1.0.0 2018-05-17
 * MODCXEKB-63: Higher API limits required changes to RM API paging.
 * MODCXEKB-61: Updated the ModuleDescriptor to contain mod-configuration
   permissions and dependencies.

## 0.0.6 2018-03-05
 * Remove the RM API query argument "selection=selected" as the default when
   the "ext.selected" query argument is not passed to the codex API. The UI has
   been updated to not pass "ext.selected" which will mean all, otherwise it
   passes "ext.selected=true" or "ext.selected=false" to represent the RM API
   query arguments "selection=selected" and "selection=notselected"
   respectively. Passing "ext.selected=all" will explicitly pass "selection=all"
   to the RM API and is the equivalent of not passing "ext.selected".

## 0.0.5 2018-02-01
 * MODCXEKB-56: Ignore unknown ext context set fields
 * If "ext.selected" is not passed in the request CQL, force
   "selection=selected" to be sent as a query parameter to the RM API.
   Returning only selected items by default was requested by the SIG.

## 0.0.4 2018-01-10
 * MODCXEKB-8: Added consumer Pact support for dependent APIs (mod-configuration and RM API).

## 0.0.3 2018-01-05
 * MODCXEKB-54: Change totalRecords to resultInfo.totalRecords

## 0.0.2 2018-01-04
 * Support instance search by ID - MODCXEKB-51 / MODCXEKB-52

## 0.0.1 2017-11-16
 * Initial work
