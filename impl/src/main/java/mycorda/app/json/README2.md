This is copied https://github.com/stleary/JSON-java at commit 00e0e6c0a295efcfb19ed42df1cb94f972b102d9

The following mods have been made
- package name changed mycorda.app.json.json to avoid collisions with any existing implementations.
- handling of Map has been improved so that
  - null values are supported and always retained 
  - keys retain the insertion / processing order (i.e. use LinkedHashMap), 
- Float point numbers always convert to BigDecimal, this
  - retains knowledge of the precision on the original input so it can be recreated on output
  - removes the chance of internal rounding errors changing the value.
  
  