import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { 
  Briefcase, 
  Calendar, 
  ExternalLink, 
  Edit,
  Trash2,
  Building,
  User,
  ChevronRight
} from 'lucide-react';

const PipelineView = ({ applications, onEdit, onDelete }) => {
  const [groupedApplications, setGroupedApplications] = useState({});

  useEffect(() => {
    const grouped = applications.reduce((acc, app) => {
      const status = app.status || 'APPLIED';
      if (!acc[status]) {
        acc[status] = [];
      }
      acc[status].push(app);
      return acc;
    }, {});
    
    setGroupedApplications(grouped);
  }, [applications]);

  const getStatusColor = (status) => {
    switch (status) {
      case 'APPLIED': return 'bg-blue-50 border-blue-200';
      case 'OA': return 'bg-yellow-50 border-yellow-200';
      case 'INTERVIEW': return 'bg-purple-50 border-purple-200';
      case 'OFFER': return 'bg-green-50 border-green-200';
      case 'REJECTED': return 'bg-red-50 border-red-200';
      default: return 'bg-gray-50 border-gray-200';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'APPLIED': return <Briefcase className="h-4 w-4 text-blue-600" />;
      case 'OA': return <Calendar className="h-4 w-4 text-yellow-600" />;
      case 'INTERVIEW': return <User className="h-4 w-4 text-purple-600" />;
      case 'OFFER': return <ExternalLink className="h-4 w-4 text-green-600" />;
      case 'REJECTED': return <Trash2 className="h-4 w-4 text-red-600" />;
      default: return <Briefcase className="h-4 w-4 text-gray-600" />;
    }
  };

  const pipelineStages = [
    { key: 'APPLIED', label: 'Applied', color: 'blue' },
    { key: 'OA', label: 'Online Assessment', color: 'yellow' },
    { key: 'INTERVIEW', label: 'Interview', color: 'purple' },
    { key: 'OFFER', label: 'Offer', color: 'green' },
    { key: 'REJECTED', label: 'Rejected', color: 'red' }
  ];

  const ApplicationCard = ({ application }) => (
    <div className={`p-3 rounded-lg border-2 ${getStatusColor(application.status)} mb-3 hover:shadow-md transition-shadow cursor-pointer`}>
      <div className="flex items-start justify-between mb-2">
        <div className="flex-1">
          <h4 className="font-semibold text-gray-900 text-sm">{application.companyName}</h4>
          <p className="text-xs text-gray-600 mt-1">{application.role}</p>
        </div>
        {getStatusIcon(application.status)}
      </div>
      
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2 text-xs text-gray-500">
          <Building className="h-3 w-3" />
          <span>{application.platform}</span>
        </div>
        <div className="flex items-center space-x-1">
          {application.jobLink && (
            <a
              href={application.jobLink}
              target="_blank"
              rel="noopener noreferrer"
              className="text-blue-600 hover:text-blue-800"
              onClick={(e) => e.stopPropagation()}
            >
              <ExternalLink className="h-3 w-3" />
            </a>
          )}
          <button
            onClick={(e) => {
              e.stopPropagation();
              onEdit(application);
            }}
            className="text-gray-600 hover:text-gray-800"
          >
            <Edit className="h-3 w-3" />
          </button>
          <button
            onClick={(e) => {
              e.stopPropagation();
              onDelete(application.id);
            }}
            className="text-red-600 hover:text-red-800"
          >
            <Trash2 className="h-3 w-3" />
          </button>
        </div>
      </div>
      
      <div className="text-xs text-gray-500 mt-2">
        Applied {format(new Date(application.appliedDate), 'MMM dd, yyyy')}
      </div>
      
      {application.source && (
        <div className="text-xs text-gray-400 mt-1">
          Source: {application.source === 'MANUAL' ? 'Manual' : 
                  application.source === 'EMAIL' ? 'Email' : 'Extension'}
        </div>
      )}
    </div>
  );

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-semibold text-gray-900">Application Pipeline</h2>
        <div className="flex items-center space-x-2 text-sm text-gray-500">
          <Briefcase className="h-4 w-4" />
          <span>{applications.length} total applications</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        {pipelineStages.map((stage) => (
          <div key={stage.key} className="min-h-96">
            <div className={`bg-${stage.color}-100 border border-${stage.color}-200 rounded-lg p-3 mb-3`}>
              <div className="flex items-center justify-between">
                <h3 className="font-medium text-gray-900 text-sm">{stage.label}</h3>
                <span className={`bg-${stage.color}-200 text-${stage.color}-800 text-xs px-2 py-1 rounded-full`}>
                  {groupedApplications[stage.key]?.length || 0}
                </span>
              </div>
            </div>
            
            <div className="space-y-2">
              {groupedApplications[stage.key]?.map((application) => (
                <ApplicationCard key={application.id} application={application} />
              ))}
              
              {(!groupedApplications[stage.key] || groupedApplications[stage.key].length === 0) && (
                <div className="text-center py-8 text-gray-400 text-sm">
                  <Briefcase className="h-8 w-8 mx-auto mb-2 opacity-50" />
                  <p>No applications in this stage</p>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      <div className="mt-6 flex items-center justify-center space-x-4 text-sm text-gray-500">
        <div className="flex items-center space-x-2">
          <ChevronRight className="h-4 w-4" />
          <span>Drag and drop to move applications between stages</span>
        </div>
      </div>
    </div>
  );
};

export default PipelineView;
