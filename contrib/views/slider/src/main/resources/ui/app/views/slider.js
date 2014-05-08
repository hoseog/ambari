/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var filters = require('views/common/filter_view');
var sort = require('views/common/sort_view');

App.SliderAppsView = App.TableView.extend({

  statusList: [
    "",
    App.SliderApp.Status.running,
    App.SliderApp.Status.frozen,
    App.SliderApp.Status.destroyed,
  ],

  content: function () {
    return this.get('controller.model');
  }.property('controller.model.length'),

  didInsertElement: function () {
    this.set('filteredContent',this.get('content'));
  },

  filteredContentInfo: function () {
    return Em.I18n.t('sliderApps.filters.info').format(this.get('filteredContent.length'), this.get('content.length'));
  }.property('content.length', 'filteredContent.length'),

  sortView: sort.wrapperView,
  nameSort: sort.fieldView.extend({
    column: 0,
    name:'name',
    displayName: "Name"
  }),

  statusSort: sort.fieldView.extend({
    column: 1,
    name:'status',
    displayName: "Status"
  }),

  typeSort: sort.fieldView.extend({
    column: 2,
    name:'appType',
    displayName: "Type"
  }),

  userSort: sort.fieldView.extend({
    column: 3,
    name:'user',
    displayName: "User"
  }),

  startSort: sort.fieldView.extend({
    column: 4,
    name:'started',
    displayName: "Start Time",
    type: "number"
  }),

  endSort: sort.fieldView.extend({
    column: 5,
    name:'ended',
    displayName: "End Time",
    type: "number"
  }),

  SliderView: Em.View.extend({
    content:null,
    tagName: 'tr'
  }),

  /**
   * Filter view for name column
   * Based on <code>filters</code> library
   */
  nameFilterView: filters.createTextView({
    column: 0,
    fieldType: 'filter-input-width',
    onChangeValue: function(){
      this.get('parentView').updateFilter(this.get('column'), this.get('value'), 'string');
    }
  }),

  statusFilterView: filters.createSelectView({
    column: 1,
    fieldType: 'filter-input-width',
    content: function() {
      return this.get('parentView.statusList');
    }.property('parentView.statusList'),
    onChangeValue: function(){
      this.get('parentView').updateFilter(this.get('column'), this.get('value'), 'string');
    }
  }),

  typeFilterView: filters.createTextView({
    column: 2,
    fieldType: 'filter-input-width',
    onChangeValue: function(){
      this.get('parentView').updateFilter(this.get('column'), this.get('value'), 'string');
    }
  }),

  userFilterView: filters.createTextView({
    column: 3,
    fieldType: 'filter-input-width',
    onChangeValue: function(){
      this.get('parentView').updateFilter(this.get('column'), this.get('value'), 'string');
    }
  }),

  startFilterView: filters.createSelectView({
    column: 4,
    fieldType: 'filter-input-width',
    content: ['', 'Past 1 hour',  'Past 1 Day', 'Past 2 Days', 'Past 7 Days', 'Past 14 Days', 'Past 30 Days', 'Custom'],
    onChangeValue: function(){
      this.get('parentView').updateFilter(this.get('column'), this.get('value'), 'date');
    },
    type: 'number'
  }),

  /**
   * associations between host property and column index
   * @type {Array}
   */
  colPropAssoc: function(){
    var associations = [];
    associations[0] = 'name';
    associations[1] = 'status';
    associations[2] = 'appType';
    associations[3] = 'user';
    associations[4] = 'started';
    associations[5] = 'ended';
    return associations;
  }.property()

});