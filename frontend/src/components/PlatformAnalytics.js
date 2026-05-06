import React from 'react';
import { 
  Linkedin, 
  Globe, 
  Search,
  Building,
  TrendingUp,
  BarChart3
} from 'lucide-react';

const PlatformAnalytics = ({ analytics }) => {
  if (!analytics || !analytics.platforms) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center justify-center h-32 text-gray-400">
          <BarChart3 className="h-8 w-8 mr-2" />
          <span>No platform data available</span>
        </div>
      </div>
    );
  }

  const getPlatformIcon = (platform) => {
    switch (platform) {
      case 'LINKEDIN': return <Linkedin className="h-5 w-5 text-blue-600" />;
      case 'NAUKRI': return <Globe className="h-5 w-5 text-orange-600" />;
      case 'INDEED': return <Search className="h-5 w-5 text-indigo-600" />;
      case 'OTHER': return <Building className="h-5 w-5 text-gray-600" />;
      default: return <Building className="h-5 w-5 text-gray-600" />;
    }
  };

  const getPlatformColor = (platform) => {
    switch (platform) {
      case 'LINKEDIN': return 'bg-blue-50 border-blue-200';
      case 'NAUKRI': return 'bg-orange-50 border-orange-200';
      case 'INDEED': return 'bg-indigo-50 border-indigo-200';
      case 'OTHER': return 'bg-gray-50 border-gray-200';
      default: return 'bg-gray-50 border-gray-200';
    }
  };

  const getPlatformBarColor = (platform) => {
    switch (platform) {
      case 'LINKEDIN': return 'bg-blue-500';
      case 'NAUKRI': return 'bg-orange-500';
      case 'INDEED': return 'bg-indigo-500';
      case 'OTHER': return 'bg-gray-500';
      default: return 'bg-gray-500';
    }
  };

  const totalApplications = Object.values(analytics.platforms).reduce((sum, platform) => sum + platform.total, 0);

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-semibold text-gray-900">Platform Analytics</h3>
        <div className="flex items-center space-x-2 text-sm text-gray-500">
          <BarChart3 className="h-4 w-4" />
          <span>{totalApplications} total applications</span>
        </div>
      </div>

      {/* Platform Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {Object.entries(analytics.platforms).map(([platform, data]) => (
          <div key={platform} className={`border-2 rounded-lg p-4 ${getPlatformColor(platform)}`}>
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center space-x-2">
                {getPlatformIcon(platform)}
                <span className="font-medium text-gray-900">{platform}</span>
              </div>
              <span className="text-2xl font-bold text-gray-900">{data.total}</span>
            </div>
            
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Responses</span>
                <span className="font-medium text-green-600">{data.responses}</span>
              </div>
              
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Response Rate</span>
                <span className="font-medium text-blue-600">{data.responseRate}%</span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Platform Comparison Chart */}
      <div className="border-t border-gray-200 pt-6">
        <h4 className="text-md font-medium text-gray-900 mb-4">Platform Performance</h4>
        
        <div className="space-y-3">
          {Object.entries(analytics.platforms)
            .sort(([,a], [,b]) => b.responseRate - a.responseRate)
            .map(([platform, data]) => (
              <div key={platform} className="flex items-center space-x-4">
                <div className="flex items-center space-x-2 w-24">
                  {getPlatformIcon(platform)}
                  <span className="text-sm font-medium text-gray-700">{platform}</span>
                </div>
                
                <div className="flex-1">
                  <div className="flex items-center space-x-2">
                    <div className="flex-1 bg-gray-200 rounded-full h-6 relative">
                      <div 
                        className={`h-6 rounded-full ${getPlatformBarColor(platform)} flex items-center justify-end pr-2`}
                        style={{ width: `${Math.max(data.responseRate, 5)}%` }}
                      >
                        {data.responseRate > 10 && (
                          <span className="text-xs text-white font-medium">
                            {data.responseRate}%
                          </span>
                        )}
                      </div>
                    </div>
                    
                    {data.responseRate <= 10 && (
                      <span className="text-xs text-gray-600 w-12 text-right">
                        {data.responseRate}%
                      </span>
                    )}
                  </div>
                </div>
                
                <div className="text-sm text-gray-600 w-20 text-right">
                  {data.responses}/{data.total}
                </div>
              </div>
            ))}
        </div>
      </div>

      {/* Insights */}
      <div className="border-t border-gray-200 pt-6 mt-6">
        <div className="flex items-center space-x-2 mb-3">
          <TrendingUp className="h-4 w-4 text-green-600" />
          <h4 className="text-md font-medium text-gray-900">Key Insights</h4>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-green-50 border border-green-200 rounded-lg p-3">
            <div className="text-sm font-medium text-green-800 mb-1">Best Performing Platform</div>
            <div className="text-lg font-bold text-green-900">
              {Object.entries(analytics.platforms)
                .sort(([,a], [,b]) => b.responseRate - a.responseRate)[0]?.[0] || 'N/A'}
            </div>
            <div className="text-xs text-green-600">
              {Object.entries(analytics.platforms)
                .sort(([,a], [,b]) => b.responseRate - a.responseRate)[0]?.[1]?.responseRate || 0}% response rate
            </div>
          </div>
          
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
            <div className="text-sm font-medium text-blue-800 mb-1">Most Used Platform</div>
            <div className="text-lg font-bold text-blue-900">
              {Object.entries(analytics.platforms)
                .sort(([,a], [,b]) => b.total - a.total)[0]?.[0] || 'N/A'}
            </div>
            <div className="text-xs text-blue-600">
              {Object.entries(analytics.platforms)
                .sort(([,a], [,b]) => b.total - a.total)[0]?.[1]?.total || 0} applications
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PlatformAnalytics;
