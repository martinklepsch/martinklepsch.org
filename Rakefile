namespace :dev do

  desc 'Start Jekyll server'
  task :go do
    system('jekyll --server --auto')
  end

  desc 'Build site'
  task :build do
    system('jekyll')
  end

  desc 'Juice up all css and js'
  task :juice do
    system('juicer merge --force stylesheets/martinklepschorg.css -o stylesheets/martinklepschorg.min.css')
  end

end

namespace :s3 do
  require 'yaml'
  require 'aws/s3'
  require 'digest/sha2'

  aws = YAML::load(open("_aws-s3.yml"))

  AWS::S3::Base.establish_connection!(
          :access_key_id => "#{aws['s3']['access_key_id']}",
          :secret_access_key => "#{aws['s3']['secret_access_key']}")

  Bucket = AWS::S3::Bucket.find(aws['s3']['bucket'])
  Bucketstring = aws['s3']['bucket']

  desc 'Send generated files to S3 bucket'
  task :deploy do

    files.each do |file|
      # sha256hash = Digest::SHA256.file(file).hexdigest
      # file_exists?(file, sha256hash)
      # require 'pry'
      # binding.pry
      s3obj = Bucket.new_object
      s3obj.key = file
      s3obj.value = open(file)
      # s3obj.metadata[:sha256] = sha256hash
      s3obj.store
      # puts "Uploaded #{file}"

      if Bucket.objects.include?(s3obj)
        puts("Sent file #{file}")
      end
    end
  end

  task :clear do
    Bucket.delete_all
  end


  def file_exists?(file, hash)
    p Bucket[file].metadata
  end

  def files
    Dir.chdir('_site')
    Dir.glob('**/*.{html,js,css,png,jpg,jpeg,pdf,woff}')
  end

end
