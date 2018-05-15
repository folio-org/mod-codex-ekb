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
