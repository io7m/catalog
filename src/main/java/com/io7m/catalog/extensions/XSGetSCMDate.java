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
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.sf.saxon.s9api.ItemType.STRING;
import static net.sf.saxon.s9api.OccurrenceIndicator.ONE;

public final class XSGetSCMDate
  implements ExtensionFunction, ProgressMonitor
{
  private static final Logger LOG =
    LoggerFactory.getLogger(XSGetSCMDate.class);

  @Override
  public QName getName()
  {
    return new QName("urn:com.io7m.catalog:extensions", "scmDateOf");
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
    throws SaxonApiException
  {
    try {
      final var arg =
        arguments[0].itemAt(0).getStringValue();

      final var tempDir =
        Files.createDirectories(Paths.get("/tmp/catalog/"));
      final var gitDir =
        tempDir.resolve(
          UUID.nameUUIDFromBytes(arg.getBytes(UTF_8)) + ".git"
        );

      try (var git = this.openOrClone(gitDir, arg)) {
        final var repository =
          git.getRepository();
        final var rw =
          new RevWalk(repository);
        final var headId =
          repository.resolve(Constants.HEAD);
        final var root =
          rw.parseCommit(headId);

        rw.sort(RevSort.REVERSE);
        rw.markStart(root);

        final var c = rw.next();
        final var authorIdent = c.getAuthorIdent();
        final var date =
          LocalDate.ofInstant(
            authorIdent.getWhenAsInstant(),
            authorIdent.getZoneId()
          );
        final var time =
          LocalTime.ofInstant(
            authorIdent.getWhenAsInstant(),
            authorIdent.getZoneId()
          );
        final var offset =
          ZoneOffset.ofHours(0);

        return new XdmAtomicValue(
          DateTimeFormatter.ISO_DATE.format(
            OffsetDateTime.of(date, time, offset)
          )
        );
      }
    } catch (final Exception e) {
      LOG.error("Failed to fetch SCM: ", e);
      throw new SaxonApiException(e);
    }
  }

  private Git openOrClone(
    final Path gitDir,
    final String targetURI)
    throws GitAPIException, IOException
  {
    if (!Files.isDirectory(gitDir)) {
      return Git.cloneRepository()
        .setBare(true)
        .setCloneAllBranches(true)
        .setGitDir(gitDir.toFile())
        .setMirror(true)
        .setURI(targetURI)
        .setProgressMonitor(this)
        .call();
    } else {
      return Git.open(gitDir.toFile());
    }
  }

  @Override
  public void start(
    final int i)
  {
    LOG.debug("Starting clone...");
  }

  @Override
  public void beginTask(
    final String task,
    final int index)
  {
    LOG.debug("{} start", task);
  }

  @Override
  public void update(
    final int i)
  {

  }

  @Override
  public void endTask()
  {

  }

  @Override
  public boolean isCancelled()
  {
    return false;
  }

  @Override
  public void showDuration(
    final boolean b)
  {

  }
}
