Simple Parameterized Builds Report plugin
=========================================

This Jenkins plugin makes it easy to visualize the various builds for a parameterized project that were run using the same set of parameters.

Usage
-----

Once installed, a new link "Simple Parameterized Builds Report" should show up in the sidebar for all parameterized jobs. Following that link will display a table where (at max, latest 10) builds for a given parameter set are displayed, grouped together based on the parameters used to invoke the builds.

Only the builds that match the parameter set used in the latest build are considered for inclusion in the table.

Release Notes
-------------

### 1.0 (01/01/2013)

* Initial release