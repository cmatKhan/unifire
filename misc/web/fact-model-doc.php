<?php

// XML
$x1 = file_get_contents("../../../../main/resources/schemas/urml-facts.xsd");
$xml_doc = new DOMDocument();
$xml_doc->loadXML("$x1");

// XSL
$xsl_doc = new DOMDocument();
$xsl_doc->load("../xsl/xs3p.xsl");

// Proc
$proc = new XSLTProcessor();
$proc->importStylesheet($xsl_doc);
$newdom = $proc->transformToDoc($xml_doc);

print $newdom->saveXML();

?>