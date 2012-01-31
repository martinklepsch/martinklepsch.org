namespace :s3 do
  require 'aws/s3'

  aws = YAML::load(open("_aws-s3.yml"))

  AWS::S3::Base.establish_connection!(
          :access_key_id => "#{aws['s3']['access_key_id']}",
          :secret_access_key => "#{aws['s3']['secret_access_key']}")

  bucket = AWS::S3::Bucket.find(aws['s3']['bucket'])

  desc 'Send generated files to S3 bucket'
  task :deploy do

    files.each do |file|
      f = bucket.new_object
      f.key = file
      f.value = open(file)
      f.store
      # AWS::S3::S3Object.store(file, open(file), bucket)
      if bucket.objects.include?(f)
        puts("Sent file #{file}")
      end
    end
  end

  task :clear do
    bucket.delete_all
  end

  def files
    Dir.chdir('_site')
    Dir.glob('**/*.{html,js,css,png,jpg,jpeg}')
  end

end
