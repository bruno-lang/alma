# alma

_understandable parsing_

### repo structure
```
alma/
+-- _data/        example input files parsed in tests
+-- alma/         several grammars specified in alma language
+-- java/         java implementation
```

### examples
JSON
```matlab
-file    = json .
-json    = object | array | bool | null | string | number
-object  = '{'< . \members? . '}'
-members = member ( . ',' . member)*
-member  = name . ':' . json
-array   = '['< . \elements? . ']'
-elements= json ( . ',' . json)*
-string  = '"' ~ '"'
-number  = ['-+']? \9* ('.' \9*)?
-bool    = 'true' | 'false'
-null    = 'null'
-name    = '"' \text '"'
-text    = [zZ9 '_-']+
```

XML
```matlab
-document   = doctype? . node@root
-doctype    = '<?xml' attributes? . '?>'
-node       = cdata | comment | element | text
-element    = '<' name attributes? . '>' . nodes? . '</' name '>'
-nodes      = node (. node)*
-attributes = (: attribute)+
-attribute  = name '=' '"' ~@value '"'
-text       = [ '<' ]^*
-cdata      = '<![CDATA[' ~@data ']]>'
-comment    = '<!--' ~@info '-->'
-name       = [zZ]+
```
