## 0.0.5 2018-02-01
 * MODCXEKB-56: Ignore unknown ext context set fields
 * If "ext.selected" is not passed in the request CQL, force
   "selection=selected" to be sent as a query parameter to the RM API.
   Returning only selected items by default was requested by the SIG.

## 0.0.4 2018-01-10
 * MODCXEKB-8: Added consumer Pact support for dependent APIs (mod-confguration and RM API).

## 0.0.3 2018-01-05
 * MODCXEKB-54: Change totalRecords to resultInfo.totalRecords

## 0.0.2 2018-01-04
 * Support instance search by ID - MODCXEKB-51 / MODCXEKB-52
 
## 0.0.1 2017-11-16
 * Initial work
