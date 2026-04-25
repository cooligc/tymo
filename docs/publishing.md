# Publishing to Maven Central

This guide explains how to publish Schedule It to Maven Central.

## Prerequisites

1. **Sonatype JIRA Account**: Create an account at [Sonatype JIRA](https://issues.sonatype.org/)
2. **GPG Key**: Generate and distribute a GPG key for signing artifacts
3. **Maven Central Approval**: Request access to publish to `io.cooligc` groupId

## GPG Setup

### Generate GPG Key

```bash
gpg --gen-key
```

### Distribute Key

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
```

## Maven Settings

Add the following to your `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-sonatype-username</username>
      <password>your-sonatype-password</password>
    </server>
  </servers>
</settings>
```

## Publishing Steps

1. **Update Version**: Change version in `pom.xml` from `1.0-SNAPSHOT` to `1.0.0`

2. **Commit Changes**:
   ```bash
   git add pom.xml
   git commit -m "Release version 1.0.0"
   git tag v1.0.0
   git push origin main --tags
   ```

3. **Deploy to OSSRH**:
   ```bash
   mvn clean deploy -P release
   ```

4. **Release from OSSRH**: Login to [OSSRH](https://s01.oss.sonatype.org/) and release the staged artifacts

5. **Update Version**: Change version to `1.0.1-SNAPSHOT` for next development

## Verification

After release, verify the artifact is available:

- [Maven Central Search](https://search.maven.org/)
- [Maven Repository](https://repo1.maven.org/maven2/io/cooligc/schedule-it/)

## Troubleshooting

### Common Issues

1. **GPG Signing Failed**: Ensure GPG key is properly configured and passphrase is set
2. **Authentication Failed**: Check OSSRH credentials in settings.xml
3. **Artifact Rejected**: Check pom.xml metadata and ensure groupId is approved

### Getting Help

- [OSSRH Guide](https://central.sonatype.org/publish/publish-guide/)
- [Maven Central Requirements](https://central.sonatype.org/publish/requirements/)</content>
<parameter name="filePath">/Users/cooligc/Documents/codes/schedule-it/docs/publishing.md