/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.jmulticlose.core.CloseableCollection;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.evt.XMLEventFactory2;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public final class AddPOMProperties
{
  private AddPOMProperties()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var filePOM =
      Paths.get(args[0]);
    final var fileOut =
      Paths.get(args[1]);

    final var eventReaders =
      XMLInputFactory2.newDefaultFactory();
    final var eventWriters =
      XMLOutputFactory2.newDefaultFactory();
    final var events =
      XMLEventFactory2.newDefaultFactory();

    final var resources =
      CloseableCollection.create();

    try (resources) {
      final var projectName =
        findProjectName(filePOM);
      final var workId =
        findWorkID(projectName);

      final var reader =
        resources.add(
          Files.newBufferedReader(filePOM));
      final var writer =
        resources.add(
          Files.newBufferedWriter(fileOut, CREATE, WRITE, TRUNCATE_EXISTING));

      final var eventWriter =
        eventWriters.createXMLEventWriter(writer);
      final var eventReader =
        eventReaders.createXMLEventReader(reader);

      while (eventReader.hasNext()) {
        final var event = eventReader.nextEvent();
        if (event instanceof StartDocument) {
          writer.append("""
                          <?xml version="1.0" encoding="UTF-8"?>
                          """.strip());
          writer.append("\n");
          writer.append("\n");
        } else {
          eventWriter.add(event);
        }

        switch (event) {
          case final StartElement st -> {
            final var name = st.getName();
            if (Objects.equals(name.getLocalPart(), "properties")) {
              eventWriter.add(
                events.createCharacters("\n    ")
              );
              eventWriter.add(
                events.createStartElement("", null, "io7m.workID")
              );
              eventWriter.add(
                events.createCharacters(workId)
              );
              eventWriter.add(
                events.createEndElement("", null, "io7m.workID")
              );
              eventWriter.add(
                events.createCharacters("\n")
              );
            }
          }
          default -> {

          }
        }

      }
    }
  }

  private static String findWorkID(
    final String projectName)
    throws Exception
  {
    final var documentBuilders =
      DocumentBuilderFactory.newDefaultNSInstance();
    final var documentBuilder =
      documentBuilders.newDocumentBuilder();
    final var document =
      documentBuilder.parse(
        "src/main/xml/catalog-io7m-core.xml"
      );
    final var works =
      document.getChildNodes();

    for (int i = 0; i < works.getLength(); ++i) {
      final var worksE = works.item(i);
      if (worksE instanceof final Element worksEE) {
        final var workList = worksEE.getChildNodes();
        for (int j = 0; j < workList.getLength(); ++j) {
          final var work = workList.item(j);
          if (work instanceof final Element workE) {
            final var workName =
              workE.getAttribute("Name");
            final var workID =
              workE.getAttribute("ID");

            if (workName.equals(projectName)) {
              return workID;
            }
          }
        }
      }
    }

    throw new NoSuchElementException(
      "No known work with name %s".formatted(projectName)
    );
  }

  private static String findProjectName(
    final Path filePOM)
    throws XPathExpressionException, IOException
  {
    final var xpaths =
      XPathFactory.newInstance();
    final var xpath =
      xpaths.newXPath();

    xpath.setNamespaceContext(new NamespaceContext()
    {
      public String getNamespaceURI(
        final String prefix)
      {
        return "mvn".equals(prefix)
          ? "http://maven.apache.org/POM/4.0.0"
          : XMLConstants.NULL_NS_URI;
      }
      public String getPrefix(
        final String uri)
      {
        return null;
      }
      public Iterator<String> getPrefixes(
        final String uri)
      {
        return null;
      }
    });

    final var expr =
      xpath.compile("/mvn:project/mvn:artifactId");

    final Element r =
      (Element) expr.evaluate(
        new InputSource(Files.newBufferedReader(filePOM)),
        XPathConstants.NODE
      );

    final var projectId = r.getTextContent().strip();
    return projectId.replace("com.io7m.", "");
  }
}
