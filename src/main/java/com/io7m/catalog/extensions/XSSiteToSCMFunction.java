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


package com.io7m.catalog.extensions;

import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static net.sf.saxon.s9api.ItemType.STRING;
import static net.sf.saxon.s9api.OccurrenceIndicator.ONE;

public final class XSSiteToSCMFunction
  implements ExtensionFunction
{
  @Override
  public QName getName()
  {
    return new QName("urn:com.io7m.catalog:extensions", "siteToSCM");
  }

  @Override
  public SequenceType getResultType()
  {
    return SequenceType.makeSequenceType(STRING, ONE);
  }

  @Override
  public SequenceType[] getArgumentTypes()
  {
    return new SequenceType[]{SequenceType.makeSequenceType(STRING, ONE)};
  }

  @Override
  public XdmValue call(
    final XdmValue[] arguments)
  {
    final String arg =
      arguments[0].itemAt(0).getStringValue();

    return new XdmAtomicValue(
      UUID.nameUUIDFromBytes(
        arg.getBytes(StandardCharsets.UTF_8)
      ).toString()
    );
  }

}
