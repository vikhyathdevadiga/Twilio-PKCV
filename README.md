# Twilio Java PKCV Examples

This repository provides Java examples showcasing the implementation of Public Key Client Validation (PKCV) for working with the Twilio API. The inspiration for these examples is derived from the [Client Validation Java Example](https://github.com/twilio/twilio-java/blob/main/src/main/java/com/twilio/example/ValidationExample.java) available in the official Twilio GitHub repository.

## Background

Public Key Client Validation (PKCV) is a security feature provided by Twilio. For a comprehensive understanding, refer to the [Twilio documentation](https://www.twilio.com/docs/iam/pkcv/quickstart).

## CustomPKCVClient

The `CustomPKCVClient` example utilizes existing credentials to authenticate the client against the Twilio API. Make sure to set the required environment variables before running the application.

### Configuration

- `PRIVATE_KEY_FILE_NAME`: Path to the private key file (e.g., `private.key`).
  Ensure this file contains the private key for authentication.
- `TWILIO_ACCOUNT_SID`: Your Twilio account SID.
- `TWILIO_API_KEY_SID`: Your Twilio API key SID.
- `TWILIO_API_KEY_SECRET`: Your Twilio API key secret.
- `TWILIO_PUBLIC_KEY_SID`: Your Twilio public key SID(CRXXXXXX).

### How to Run

Compile and run the `CustomPKCVClient` class to validate the client.

## CustomPKCVClient1

The `CustomPKCVClient1` example generates required credentials and then authenticates the client against the Twilio API.

### Configuration

Make sure to set the following environment variables before running the application:

- `TWILIO_ACCOUNT_SID`: Your Twilio account SID.
- `TWILIO_AUTH_TOKEN`: Your Twilio authentication token.

### How to Run

Compile and run the `CustomPKCVClient1` class to create necessary credentials and authenticate the client.

## Reference

For detailed information, you can refer to the original [Twilio documentation](https://www.twilio.com/docs/iam/pkcv/quickstart).

## License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/vikhyathdevadiga/Twilio-PKCV/blob/master/LICENSE) file for details.
