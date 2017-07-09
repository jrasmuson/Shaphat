gradle buildZip && \
aws s3 --profile shaphat_test cp build/distributions/Shaphat-1.0.zip s3://jacobr-shaphat-test && \
aws --profile shaphat_test lambda update-function-code --function-name "shaphat_test_lambda" --s3-bucket "jacobr-shaphat-test" --s3-key "Shaphat-1.0.zip"