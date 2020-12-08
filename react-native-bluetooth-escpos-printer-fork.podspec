require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-bluetooth-escpos-printer-fork"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/wiyantotan/react-native-bluetooth-escpos-printer-fork.git", :tag => "#{s.version}" }


  s.source_files = "ios/**/*.{h,m,mm}"

  s.dependency 'ZXingObjC', '~> 3.6.4'
  s.dependency "React"
end
