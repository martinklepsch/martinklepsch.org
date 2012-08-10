class Tools < Thor

  desc 'build', 'run jekyll to generate static site'
  def build
    system('jekyll')
  end

  desc 'go', 'start jekyll development server'
  def go
    system('jekyll --server --auto')
  end

  desc 'deploy', 'push changes to S3'
  def deploy
    require 'yaml'
    require 'aws/s3'
    require 'digest/sha2'

    aws = YAML::load(open("_aws-s3.yml"))

    AWS::S3::Base.establish_connection!(
    :access_key_id => "#{aws['s3']['access_key_id']}",
    :secret_access_key => "#{aws['s3']['secret_access_key']}")

    bucket = AWS::S3::Bucket.find(aws['s3']['bucket'])
    bucketstring = aws['s3']['bucket']

    files.each do |file|
      # sha256hash = Digest::SHA256.file(file).hexdigest
      # file_exists?(file, sha256hash)
      # require 'pry'
      # binding.pry
      s3obj = bucket.new_object
      s3obj.key = file
      s3obj.value = open(file)
      # s3obj.metadata[:sha256] = sha256hash
      s3obj.store
      # puts "Uploaded #{file}"

      if bucket.objects.include?(s3obj)
        puts("Sent file #{file}")
      end
    end
  end



  no_tasks do
    def files
      Dir.chdir('_site')
      Dir.glob('**/*.{xml,html,js,css,png,jpg,jpeg,ico,pdf,woff}')
    end
  end
end
