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

import com.io7m.catalog.extensions.XSFunctions;
import com.io7m.catalog.extensions.XSHashUUIDFunction;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.Boolean.TRUE;
import static net.sf.saxon.lib.FeatureKeys.OPTIMIZATION_LEVEL;
import static net.sf.saxon.lib.FeatureKeys.XINCLUDE;
import static net.sf.saxon.lib.FeatureKeys.XSLT_ENABLE_ASSERTIONS;

public final class Site
{
  private Site()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var fileSrc =
      Paths.get(args[0]);
    final var fileOut =
      Paths.get(args[1]);

    final var configuration = new Configuration();
    configuration.setConfigurationProperty(OPTIMIZATION_LEVEL, "ltmv");
    configuration.setConfigurationProperty(XINCLUDE, TRUE);
    configuration.setConfigurationProperty(XSLT_ENABLE_ASSERTIONS, TRUE);

    final var processor = new Processor(configuration);
    for (final var f : XSFunctions.FUNCTIONS) {
      processor.registerExtensionFunction(f);
    }

    final var compiler =
      processor.newXsltCompiler();

    final var executable =
      compileStylesheet(
        Site.class.getResource("/com/io7m/catalog/site.xsl"),
        compiler
      );
    final var transformer =
      executable.load();

    try (var stream = Files.newInputStream(fileSrc)) {
      final var source = new InputSource();
      source.setByteStream(stream);
      source.setSystemId(fileSrc.toString());
      transformer.setSource(new SAXSource(source));

      final var out = processor.newSerializer(fileOut.toFile());
      out.setOutputProperty(Serializer.Property.METHOD, "xml");
      out.setOutputProperty(Serializer.Property.INDENT, "yes");
      transformer.setDestination(out);
      transformer.transform();
    }
  }

  private static XsltExecutable compileStylesheet(
    final URL target,
    final XsltCompiler compiler)
    throws IOException, SaxonApiException
  {
    final XsltExecutable executable;
    try (var stream = target.openStream()) {
      final var source = new InputSource();
      source.setByteStream(stream);
      source.setSystemId(target.toString());
      executable = compiler.compile(new SAXSource(source));
    }
    return executable;
  }
}
