cat orchidTest.json |xargs -0 aws --profile shaphat_test kinesis put-record --stream-name shaphat_test_stream --partition-key test3 --data