import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import { 
  Calendar, 
  Briefcase, 
  User, 
  FileText,
  CheckCircle,
  XCircle,
  Clock,
  ExternalLink
} from 'lucide-react';

const TimelineView = ({ applicationId }) => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (applicationId) {
      fetchTimelineEvents();
    }
  }, [applicationId]);

  const fetchTimelineEvents = async () => {
    try {
      const response = await fetch(`http://localhost:8082/api/stats/timeline/${applicationId}`);
      if (!response.ok) throw new Error('Failed to fetch timeline events');
      
      const data = await response.json();
      setEvents(data);
    } catch (error) {
      console.error('Error fetching timeline events:', error);
    } finally {
      setLoading(false);
    }
  };

  const getEventIcon = (eventType) => {
    switch (eventType) {
      case 'APPLIED': return <Briefcase className="h-4 w-4 text-blue-600" />;
      case 'OA': return <FileText className="h-4 w-4 text-yellow-600" />;
      case 'INTERVIEW': return <User className="h-4 w-4 text-purple-600" />;
      case 'OFFER': return <CheckCircle className="h-4 w-4 text-green-600" />;
      case 'REJECTED': return <XCircle className="h-4 w-4 text-red-600" />;
      case 'STATUS_UPDATED': return <Clock className="h-4 w-4 text-gray-600" />;
      case 'NOTE_ADDED': return <FileText className="h-4 w-4 text-gray-600" />;
      default: return <Calendar className="h-4 w-4 text-gray-600" />;
    }
  };

  const getEventColor = (eventType) => {
    switch (eventType) {
      case 'APPLIED': return 'bg-blue-100 border-blue-300';
      case 'OA': return 'bg-yellow-100 border-yellow-300';
      case 'INTERVIEW': return 'bg-purple-100 border-purple-300';
      case 'OFFER': return 'bg-green-100 border-green-300';
      case 'REJECTED': return 'bg-red-100 border-red-300';
      case 'STATUS_UPDATED': return 'bg-gray-100 border-gray-300';
      case 'NOTE_ADDED': return 'bg-gray-100 border-gray-300';
      default: return 'bg-gray-100 border-gray-300';
    }
  };

  const getEventLabel = (eventType) => {
    switch (eventType) {
      case 'APPLIED': return 'Application Submitted';
      case 'OA': return 'Online Assessment';
      case 'INTERVIEW': return 'Interview';
      case 'OFFER': return 'Offer Received';
      case 'REJECTED': return 'Application Rejected';
      case 'STATUS_UPDATED': return 'Status Updated';
      case 'NOTE_ADDED': return 'Note Added';
      default: return 'Event';
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-32">
        <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-semibold text-gray-900">Application Timeline</h3>
        <div className="flex items-center space-x-2 text-sm text-gray-500">
          <Calendar className="h-4 w-4" />
          <span>{events.length} events</span>
        </div>
      </div>

      {events.length === 0 ? (
        <div className="text-center py-8 text-gray-400">
          <Calendar className="h-12 w-12 mx-auto mb-3 opacity-50" />
          <p>No timeline events available</p>
        </div>
      ) : (
        <div className="relative">
          {/* Timeline line */}
          <div className="absolute left-6 top-0 bottom-0 w-0.5 bg-gray-200"></div>
          
          <div className="space-y-4">
            {events.map((event, index) => (
              <div key={event.id || index} className="relative flex items-start space-x-4">
                {/* Timeline dot */}
                <div className={`relative z-10 flex items-center justify-center w-12 h-12 rounded-full border-2 ${getEventColor(event.eventType)}`}>
                  {getEventIcon(event.eventType)}
                </div>
                
                {/* Event content */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between mb-1">
                    <h4 className="text-sm font-medium text-gray-900">
                      {getEventLabel(event.eventType)}
                    </h4>
                    <span className="text-xs text-gray-500">
                      {format(new Date(event.timestamp), 'MMM dd, yyyy HH:mm')}
                    </span>
                  </div>
                  
                  {event.description && (
                    <p className="text-sm text-gray-600 mb-2">{event.description}</p>
                  )}
                  
                  {/* Additional event details */}
                  {event.eventType === 'STATUS_UPDATED' && event.description && (
                    <div className="bg-gray-50 rounded p-2 text-xs text-gray-700">
                      <div className="flex items-center space-x-1">
                        <Clock className="h-3 w-3" />
                        <span>Status change detected</span>
                      </div>
                    </div>
                  )}
                  
                  {event.eventType === 'INTERVIEW' && (
                    <div className="bg-purple-50 rounded p-2 text-xs text-purple-700">
                      <div className="flex items-center space-x-1">
                        <User className="h-3 w-3" />
                        <span>Interview scheduled</span>
                      </div>
                    </div>
                  )}
                  
                  {event.eventType === 'OFFER' && (
                    <div className="bg-green-50 rounded p-2 text-xs text-green-700">
                      <div className="flex items-center space-x-1">
                        <CheckCircle className="h-3 w-3" />
                        <span>Congratulations! Offer received</span>
                      </div>
                    </div>
                  )}
                  
                  {event.eventType === 'REJECTED' && (
                    <div className="bg-red-50 rounded p-2 text-xs text-red-700">
                      <div className="flex items-center space-x-1">
                        <XCircle className="h-3 w-3" />
                        <span>Application not selected</span>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
      
      {/* Timeline summary */}
      {events.length > 0 && (
        <div className="mt-6 pt-4 border-t border-gray-200">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">
                {events.filter(e => e.eventType === 'APPLIED').length}
              </div>
              <div className="text-xs text-gray-500">Applied</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-yellow-600">
                {events.filter(e => e.eventType === 'OA').length}
              </div>
              <div className="text-xs text-gray-500">Assessments</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">
                {events.filter(e => e.eventType === 'INTERVIEW').length}
              </div>
              <div className="text-xs text-gray-500">Interviews</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">
                {events.filter(e => e.eventType === 'OFFER').length}
              </div>
              <div className="text-xs text-gray-500">Offers</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TimelineView;
