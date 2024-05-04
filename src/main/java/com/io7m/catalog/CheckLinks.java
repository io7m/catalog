/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public final class CheckLinks
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CheckLinks.class);

  private CheckLinks()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var documents =
      DocumentBuilderFactory.newDefaultNSInstance();

    documents.setXIncludeAware(true);

    final var documentBuilder =
      documents.newDocumentBuilder();
    final var document =
      documentBuilder.parse(new File(args[0]));

    final var namespaces = new SimpleNamespaceContext();
    namespaces.prefixes.put("c", "urn:com.io7m.catalog:1");

    final var urisToCheck = new HashSet<URI>();

    {
      final var xPathFactory =
        XPathFactory.newInstance();
      final var xPath =
        xPathFactory.newXPath();

      xPath.setNamespaceContext(namespaces);

      final var nodes =
        (NodeList) xPath.evaluate(
          "//c:Meta[@Name='site']",
          document,
          XPathConstants.NODESET
        );

      for (var i = 0; i < nodes.getLength(); i++) {
        final var node = nodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          urisToCheck.add(URI.create(node.getTextContent()));
        }
      }
    }

    {
      final var xPathFactory =
        XPathFactory.newInstance();
      final var xPath =
        xPathFactory.newXPath();

      xPath.setNamespaceContext(namespaces);

      final var nodes =
        (NodeList) xPath.evaluate(
          "//c:Meta[@Name='source_scm']",
          document,
          XPathConstants.NODESET
        );

      for (var i = 0; i < nodes.getLength(); i++) {
        final var node = nodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          urisToCheck.add(URI.create(node.getTextContent()));
        }
      }
    }

    final var badURIs = new TreeMap<URI, String>();
    for (final var uri : urisToCheck) {
      LOG.debug("Check {}", uri);

      try (var http = HttpClient.newHttpClient()) {
        final var response =
          http.send(
            HttpRequest.newBuilder()
              .HEAD()
              .uri(uri)
              .build(),
            HttpResponse.BodyHandlers.discarding()
          );

        if (response.statusCode() >= 400) {
          badURIs.put(
            uri,
            String.format("%s", Integer.valueOf(response.statusCode()))
          );
        }
      }
    }

    if (!badURIs.isEmpty()) {
      for (final var entry : badURIs.entrySet()) {
        LOG.error("{} {}", entry.getKey(), entry.getValue());
      }
      throw new IllegalStateException("One or more URIs failed.");
    }
  }

  private static final class SimpleNamespaceContext
    implements NamespaceContext
  {
    private final Map<String, String> prefixes =
      new HashMap<>();

    SimpleNamespaceContext()
    {

    }

    @Override
    public String getNamespaceURI(
      final String prefix)
    {
      return this.prefixes.get(prefix);
    }

    @Override
    public String getPrefix(
      final String uri)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterator getPrefixes(
      final String uri)
    {
      throw new UnsupportedOperationException();
    }

  }
}
