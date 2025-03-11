import sys
import os

module_path = os.path.abspath(os.getcwd() + '\\..')
if module_path not in sys.path:
    sys.path.append(module_path)

import pandas as pd
import numpy as np
from mlxtend.frequent_patterns import apriori, association_rules

#Step1: load data

df = pd.read_csv("Experiment1.csv", names = ['Layers'], sep = ',')
#df = pd.read_csv("Experiment2.csv", names = ['Layers'], sep = ',')
#df.head()

index = df.index
number_of_deviations = len(index)
print('number of deviations is:')
print(number_of_deviations)

data = list(df["Layers"].apply(lambda x:x.split(",") ))

#Step2: Transform data with one-hot encoding
from mlxtend.preprocessing import TransactionEncoder
a = TransactionEncoder()
a_data = a.fit(data).transform(data)
df = pd.DataFrame(a_data,columns=a.columns_)
df = df.replace(False,0)
#df.to_csv('converted.txt', index=True, sep=';')


#Step3: Applying apriori algorithm

#Settings for Experiment1
frequent_itemsets = apriori(df, min_support = 0.01, use_colnames = True)

#Settings for Experiment2
#frequent_itemsets = apriori(df, min_support = 0.001, use_colnames = True)
#frequent_itemsets

#Step4: Using the Associan rule mining.
rm_results = association_rules(frequent_itemsets, metric = "support", min_threshold = 0.001)
#rm_results
#rm_results.to_csv('ruleminingAllResults.txt', index=True, sep=';')
#rm_results.shape
rm_results['alength'] = rm_results['antecedents'].apply(lambda x: len(x))
rm_results['clength'] = rm_results['consequents'].apply(lambda x: len(x))


#Step5: Filtering and Automatic Interpretation of relative Rules
#performing single and multi-dimensional analysis

#System Behavior Level 1:
#we are only interested in consequents that icludes one type of deviations
filtered_result_1=rm_results[(rm_results['alength'] == 1) & (rm_results['clength'] == 1) & (rm_results['consequents'].astype(str).str.contains('dev:'))]


#**********************Select threshold*************************************
support_threshold=0.001

filtered_result_1 = filtered_result_1[ filtered_result_1['support'] >= support_threshold ]


filtered_result_1.reset_index(drop=True, inplace=True)
filtered_result_1.to_csv('filtered_result_1.txt', index=True, sep=';')

ruleminingResult=[]
ruleminingResult_Role=[]

#Step6: automatically interpretation of the results

for index, row in filtered_result_1.iterrows():
    x_antecedents=filtered_result_1.loc[index].at["antecedents"]
    x_consequents=filtered_result_1.loc[index].at["consequents"]
    x_antecedentSupport=int(filtered_result_1.loc[index].at["antecedent support"]*(number_of_deviations))
    x_consequentSupport=int(filtered_result_1.loc[index].at["consequent support"]*(number_of_deviations))
    x_Support=int(filtered_result_1.loc[index].at["support"]*(number_of_deviations))
    x_Support1=filtered_result_1.loc[index].at["support"]
    x_confidence=filtered_result_1.loc[index].at["confidence"]
    x_activity =''
    x_operation=''
    x_role=''
    x_erole=''
    x_user=''
    x_interpretation=''
    sortedAntecedents=''
    sortedAntecedents2=''
    
    fs1 = x_antecedents
    for x in fs1:
        behavior=str(x) 
        if(behavior.startswith('a:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('ca:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('PLa:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('d:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('cd:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('DLd:')):
             x_operation=x.split(':')[1]
        if(behavior.startswith('r:')):
            x_role=x.split(':')[1]
        if(behavior.startswith('r_e:')):
            x_erole=x.split(':')[1]
        if(behavior.startswith('u:')):
            x_user=x.split(':')[1] 

    if(x_erole.startswith('*NN')):
        x_erole=''
    if(x_operation.startswith('*NA')):
        x_operation=''
    if(x_activity.startswith('*NA')):
        x_activity=''
    if(x_role.startswith('*NA')):
        x_role='' 
    sortedAntecedents=x_operation+'_'+x_activity+'_'+x_role+'_'+x_user
    sortedAntecedents2=x_operation+'_'+x_activity+'_'+x_erole+'_'+x_user
    
    fs2 = x_consequents
    for y in fs2:
        deviation=str(y) 
        if(deviation.startswith('dev:DL') & behavior.startswith('DLd:')):
            interpretation='Unexpected data operation '+str(x_operation)+ ' was executed '+str(x_Support)+' times.'
            ruleminingResult.append([interpretation,'Data Layer',x_Support,x_Support1, x_confidence ,'NaE_'+sortedAntecedents , 'T1'])
        if(deviation.startswith('dev:PL') & behavior.startswith('PLa:')):
            interpretation='Unexpected activity '+str(x_activity)+' was performed '+str(x_Support)+' times.'
            ruleminingResult.append([interpretation,'Process Layer',x_Support,x_Support1, x_confidence ,sortedAntecedents,'T1'])
        if(deviation.startswith('dev:TSMP') & behavior.startswith('cd:')):
            interpretation= 'Mandatory data operation '+str(x_operation)+ ' was illegally executed'+str(x_Support)+' times.'
            ruleminingResult.append([interpretation,'Privacy Layer',x_Support,x_Support1, x_confidence ,sortedAntecedents,'T1'])      
        if(deviation.startswith('dev:PSMP') & behavior.startswith('ca:') &  (r'*NA' not in behavior) & behavior.find('_')):
            interpretation='Activity '+str(x_activity)+' was illegally performed '+str(x_Support)+' times.'
            ruleminingResult.append([interpretation,'Privacy Layer',x_Support,x_Support1, x_confidence ,sortedAntecedents,'T1']) 
        if(deviation.startswith('dev:MMa') &  behavior.startswith('a:')):
            interpretation='Activity '+str(x_activity)+' was skipped '+str(x_Support)+' times'
            ruleminingResult.append([interpretation,'Process Layer',x_Support,x_Support1, x_confidence ,'Nd_'+sortedAntecedents,'T1'])
        if( (deviation.startswith('dev:MMdm') ) & (behavior.startswith('d:'))):
            interpretation='Mandatory data Operation '+str(x_operation) +' was ignored '+str(x_Support)+ ' times.'
            ruleminingResult.append([interpretation ,'Data Layer',x_Support,x_Support1, x_confidence ,'NaM_'+sortedAntecedents,'T1'])
        if(deviation.startswith('dev:MMdo') & behavior.startswith('d:')):
            interpretation='Optional data Operation '+str(x_operation) +' was ignored '+str(x_Support)+ ' times.'
            ruleminingResult.append([interpretation,'Data Layer',x_Support,x_Support1, x_confidence ,sortedAntecedents,'T1'])

#System Behavior level2
filtered_result_2=rm_results[(rm_results['alength'] == 2) & (rm_results['clength'] == 1) & (rm_results['consequents'].astype(str).str.contains('dev:'))]



filtered_result_2 = filtered_result_2[ filtered_result_2['support'] >= support_threshold ]


filtered_result_2.reset_index(drop=True, inplace=True)
filtered_result_2.to_csv('filtered_result_2.txt', index=True, sep=';')

#Step6: automatically interpretation of the results

for index, row in filtered_result_2.iterrows():
    x_antecedents=filtered_result_2.loc[index].at["antecedents"]
    x_consequents=filtered_result_2.loc[index].at["consequents"]
    x_antecedentSupport=int(filtered_result_2.loc[index].at["antecedent support"]*(number_of_deviations))
    x_consequentSupport=int(filtered_result_2.loc[index].at["consequent support"]*(number_of_deviations))
    x_Support=int(filtered_result_2.loc[index].at["support"]*(number_of_deviations))
    x_Support1=filtered_result_2.loc[index].at["support"]
    x_confidence=filtered_result_2.loc[index].at["confidence"]
    x_activity =''
    x_operation=''
    x_role=''
    x_erole=''
    x_user=''
    x_interpretation=''
    sortedAntecedents='' 
    sortedAntecedents2=''
    
    fs1 = x_antecedents
    for x in fs1:
        behavior=str(x) 
        if(behavior.startswith('a:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('ca:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('PLa:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('d:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('cd:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('DLd:')):
             x_operation=x.split(':')[1]
        if(behavior.startswith('r:')):
            x_role=x.split(':')[1]
        if(behavior.startswith('r_e:')):
            x_erole=x.split(':')[1]
        if(behavior.startswith('u:')):
            x_user=x.split(':')[1] 

    if(x_erole.startswith('*NN')):
        x_erole=''
    if(x_operation.startswith('*NA')):
        x_operation=''
    if(x_activity.startswith('*NA')):
        x_activity=''
    if(x_role.startswith('*NA')):
        x_role='' 
    sortedAntecedents=x_operation+'_'+x_activity+'_'+x_role+'_'+x_user
    sortedAntecedents2=x_operation+'_'+x_activity+'_'+x_erole+'_'+x_user
    
    fs2 = x_consequents
    for y in fs2:
        deviation=str(y) 
        if(deviation.startswith('dev:MMdm') & (x_activity !='') & (x_operation !='')):
            interpretation='    +'+str(x_Support)+ ' times '+' in the context of activity '+x_activity+'.'
            ruleminingResult.append([interpretation ,'Data Layer & Process Layer', x_Support,x_Support1, x_confidence ,'NaM_'+sortedAntecedents,'T2'])
        if(deviation.startswith('dev:MMdo') & (x_activity != '') & (x_operation !='')):
            interpretation='    +'+str(x_Support)+ ' times '+' in the context of activity '+x_activity+'.'
            ruleminingResult.append([interpretation,'Data Layer & Process Layer', x_Support,x_Support1, x_confidence ,sortedAntecedents,'T2'])
        if(deviation.startswith('dev:TSMP') & (x_operation !='') & (x_activity !='')):
            interpretation='    +'+str(x_Support)+' times '+ ' in the context of activity  '+x_activity+'.'
            ruleminingResult.append([interpretation,'Data Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,sortedAntecedents2,'T2'])    
        if(deviation.startswith('dev:DL') & (x_operation !='') & (x_erole !='')):
            interpretation='    +'+str(x_Support)+' times '+ ' by the role '+x_erole+'.'
            ruleminingResult.append([interpretation ,'Data Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,'NaE_'+sortedAntecedents2+'Z','T2'])
        if(deviation.startswith('dev:PSMP') & (x_activity !='') & (x_erole !='')):
            interpretation='    +'+str(x_Support)+' times '+ ' by the role '+x_erole+'.'
            ruleminingResult.append([interpretation ,'Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,sortedAntecedents2+'Z','T2']) 
        if(deviation.startswith('dev:PL') & (x_activity !='') & (x_erole !='')):
            interpretation='    +'+str(x_Support)+' times '+ ' by the role '+x_erole+'.'
            ruleminingResult.append([interpretation ,'Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,sortedAntecedents2+'Z','T2'])
        if(deviation.startswith('dev:MMa') & (x_activity !='') & (x_role !='')):
            interpretation='    -'+str(x_Support)+' times '+ ' by the role '+x_role+'.'
            ruleminingResult.append([interpretation ,'Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,'Nd_'+sortedAntecedents+'Z','T2'])

#System Behavior level3
filtered_result_3=rm_results[(rm_results['alength'] == 3) & (rm_results['clength'] == 1) & (rm_results['consequents'].astype(str).str.contains('dev:'))]


filtered_result_3 = filtered_result_3[ filtered_result_3['support'] >= support_threshold ]


filtered_result_3.reset_index(drop=True, inplace=True)
filtered_result_3.to_csv('filtered_result_3.txt', index=True, sep=';')

#Step6: automatically interpretation of the results

for index, row in filtered_result_3.iterrows():
    x_antecedents=filtered_result_3.loc[index].at["antecedents"]
    x_consequents=filtered_result_3.loc[index].at["consequents"]
    x_antecedentSupport=int(filtered_result_3.loc[index].at["antecedent support"]*(number_of_deviations))
    x_consequentSupport=int(filtered_result_3.loc[index].at["consequent support"]*(number_of_deviations))
    x_Support=int(filtered_result_3.loc[index].at["support"]*(number_of_deviations))
    x_Support1=filtered_result_3.loc[index].at["support"]
    x_confidence=filtered_result_3.loc[index].at["confidence"]
    x_activity =''
    x_operation=''
    x_role=''
    x_erole=''
    x_user=''
    x_interpretation=''
    sortedAntecedents='' 
    sortedAntecedents2=''
    
    fs1 = x_antecedents
    for x in fs1:
        behavior=str(x) 
        if(behavior.startswith('a:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('ca:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('PLa:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('d:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('cd:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('DLd:')):
             x_operation=x.split(':')[1]
        if(behavior.startswith('r:')):
            x_role=x.split(':')[1]
        if(behavior.startswith('r_e:')):
            x_erole=x.split(':')[1]
        if(behavior.startswith('u:')):
            x_user=x.split(':')[1] 

    if(x_erole.startswith('*NN')):
        x_erole=''
    if(x_operation.startswith('*NA')):
        x_operation=''
    if(x_activity.startswith('*NA')):
        x_activity=''
    if(x_role.startswith('*NA')):
        x_role='' 
    sortedAntecedents=x_operation+'_'+x_activity+'_'+x_role+'_'+x_user
    sortedAntecedents2=x_operation+'_'+x_activity+'_'+x_erole+'_'+x_user
    
    fs2 = x_consequents
    for y in fs2:
        deviation=str(y) 
        if(deviation.startswith('dev:DL') & (x_user !='') & (x_operation !='') & (x_erole !='')):
            interpretation='         -'+str(x_Support)+' times '+ ' by the user '+x_user+'.'
            ruleminingResult.append([interpretation,'Data Layer & Privacy Layer', x_Support,x_Support1, x_confidence , 'NaE_'+sortedAntecedents2,'T3'])
        if(deviation.startswith('dev:PL') & (x_user !='') & (x_activity !='') & (x_erole !='')):
            interpretation='         -'+str(x_Support)+' times '+ ' by the user '+x_user+'.'
            ruleminingResult.append([interpretation,'Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence , sortedAntecedents2,'T3'])
        if(deviation.startswith('dev:PSMP') & (x_user !='') & (x_activity !='') & (x_erole !='')):
            interpretation='         -'+str(x_Support)+' times '+ ' by the user '+x_user+'.'
            ruleminingResult.append([interpretation,'Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence , sortedAntecedents2,'T3'])
        if(deviation.startswith('dev:TSMP') & (x_activity !='') & (x_operation !='') & (x_erole !='')):
            interpretation='         +'+str(x_Support)+' times '+ ' by the role '+x_erole+'.'
            ruleminingResult.append([interpretation,'DataLayer & Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence , sortedAntecedents2,'T3'])
        if(deviation.startswith('dev:MMdm') & (x_activity !='') & (x_operation !='') & (x_role !='')):
            interpretation='         -'+str(x_Support)+' times '+ ' by the role '+x_role+'.'
            ruleminingResult.append([interpretation,'DataLayer & Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence , 'NaM_'+sortedAntecedents,'T3'])
        if(deviation.startswith('dev:MMdo') & (x_activity !='') & (x_operation !='') & (x_role !='')):
            interpretation='         -'+str(x_Support)+' times '+ ' by the role '+x_role+'.'
            ruleminingResult.append([interpretation,'DataLayer & Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence , sortedAntecedents,'T3'])

#System Behavior level4
filtered_result_4=rm_results[(rm_results['alength'] == 4) & (rm_results['clength'] == 1) & (rm_results['consequents'].astype(str).str.contains('dev:'))]

filtered_result_4 = filtered_result_4[ filtered_result_4['support'] >= support_threshold ]


filtered_result_4.reset_index(drop=True, inplace=True)
filtered_result_4.to_csv('filtered_result_4.txt', index=True, sep=';')

#Step6: automatically interpretation of the results

for index, row in filtered_result_4.iterrows():
    x_antecedents=filtered_result_4.loc[index].at["antecedents"]
    x_consequents=filtered_result_4.loc[index].at["consequents"]
    x_antecedentSupport=int(filtered_result_4.loc[index].at["antecedent support"]*(number_of_deviations))
    x_consequentSupport=int(filtered_result_4.loc[index].at["consequent support"]*(number_of_deviations))
    x_Support=int(filtered_result_4.loc[index].at["support"]*(number_of_deviations))
    x_Support1=filtered_result_4.loc[index].at["support"]
    x_confidence=filtered_result_4.loc[index].at["confidence"]
    x_activity =''
    x_operation=''
    x_role=''
    x_erole=''
    x_user=''
    x_interpretation=''
    sortedAntecedents='' 
    sortedAntecedents2=''
    
    fs1 = x_antecedents
    for x in fs1:
        behavior=str(x) 
        if(behavior.startswith('a:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('ca:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('PLa:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('d:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('cd:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('DLd:')):
             x_operation=x.split(':')[1]
        if(behavior.startswith('r:')):
            x_role=x.split(':')[1]
        if(behavior.startswith('r_e:')):
            x_erole=x.split(':')[1]
        if(behavior.startswith('u:')):
            x_user=x.split(':')[1] 
        #print(x_activity, x_operation,x_role,x_user)

    if(x_erole.startswith('*NN')):
        x_erole=''
    if(x_operation.startswith('*NA')):
        x_operation=''
    if(x_activity.startswith('*NA')):
        x_activity=''
    if(x_role.startswith('*NA')):
        x_role='' 
    sortedAntecedents=x_operation+'_'+x_activity+'_'+x_role+'_'+x_user
    sortedAntecedents2=x_operation+'_'+x_activity+'_'+x_erole+'_'+x_user
    
    fs2 = x_consequents
    for y in fs2:
        deviation=str(y) 
        if(deviation.startswith('dev:TSMP') & (x_user!='') ):
            interpretation='              -'+str(x_Support)+' times '+ ' by the user '+x_user+'.'
            ruleminingResult.append([interpretation ,'Data Layer & Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,sortedAntecedents2,'T4'])


#Step7: Categorizing the results (System Deviating Patterns)
ruleminingResult
df_interpretations= pd.DataFrame(ruleminingResult,columns=['Deviating Behaviors', 'Perspective(s)','occurrence','Support','confidence','behavior','TreeStructure'])
df_interpretations.to_csv('test.csv', index=True, sep=';')
df_interpretations.sort_values(by=['behavior'], inplace=True, ascending = [False])
df_interpretations.reset_index(drop=True, inplace=True)
df_SystemDeviatingPattern=df_interpretations[['Deviating Behaviors','occurrence','Support','confidence']] 
df_SystemDeviatingPattern.to_csv('SystemDeviatingPatterns.csv', index=True, sep=';')


#User Behavior Level 1:
ruleminingResult_User=[]

#Step6: automatically interpretation of the results

for index, row in filtered_result_1.iterrows():
    x_antecedents=filtered_result_1.loc[index].at["antecedents"]
    x_consequents=filtered_result_1.loc[index].at["consequents"]
    x_antecedentSupport=int(filtered_result_1.loc[index].at["antecedent support"]*(number_of_deviations))
    x_consequentSupport=int(filtered_result_1.loc[index].at["consequent support"]*(number_of_deviations))
    x_Support=int(filtered_result_1.loc[index].at["support"]*(number_of_deviations))
    x_Support1=filtered_result_1.loc[index].at["support"]
    x_confidence=filtered_result_1.loc[index].at["confidence"]
    x_activity =''
    x_operation=''
    x_role=''
    x_erole=''
    x_user=''
    x_interpretation=''
    sortedAntecedents='' 
    sortedAntecedents2=''
    
    fs1 = x_antecedents
    for x in fs1:
        behavior=str(x) 
        if(behavior.startswith('a:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('ca:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('PLa:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('d:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('cd:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('DLd:')):
             x_operation=x.split(':')[1]
        if(behavior.startswith('r:')):
            x_role=x.split(':')[1]
        if(behavior.startswith('r_e:')):
            x_erole=x.split(':')[1]
        if(behavior.startswith('u:')):
            x_user=x.split(':')[1] 

    if(x_erole.startswith('*NN')):
        x_erole=''
    if(x_operation.startswith('*NA')):
        x_operation=''
    if(x_activity.startswith('*NA')):
        x_activity=''
    if(x_role.startswith('*NA')):
        x_role='' 
    sortedAntecedents=x_operation+'_'+x_activity+'_'+x_role+'_'+x_user
    sortedAntecedents2=x_operation+'_'+x_activity+'_'+x_erole+'_'+x_user
    
    fs2 = x_consequents
    for y in fs2:
        deviation=str(y) 
        if(deviation.startswith('dev:DL') & behavior.startswith('u:')):
            interpretation='The user '+str(x_user)+ ' executed unexpected data operations '+str(x_Support)+' times.'
            ruleminingResult_User.append([interpretation,'Data Layer',x_Support,x_Support1, x_confidence ,'NaE_'+sortedAntecedents , 'T1',str(x_user),'DL'])
        if(deviation.startswith('dev:PL') & behavior.startswith('u:')):
            interpretation='The user '+str(x_user)+ ' perfomed unexpected activities '+str(x_Support)+' times.'
            ruleminingResult_User.append([interpretation,'Process Layer',x_Support,x_Support1, x_confidence ,sortedAntecedents,'T1',str(x_user),'PL'])
        if(deviation.startswith('dev:TSMP') & behavior.startswith('u:')):
            interpretation= 'The user '+str(x_user)+ ' illegally executed expected data operations '+str(x_Support)+' times.'
            ruleminingResult_User.append([interpretation,'Privacy Layer',x_Support,x_Support1, x_confidence ,sortedAntecedents,'T1',str(x_user),'TSMP'])       
        if(deviation.startswith('dev:PSMP') & behavior.startswith('u:') &  (r'*NA' not in behavior) & behavior.find('_')):
            interpretation='The user '+str(x_user)+ ' illegally performed expected activites'+str(x_Support)+' times.'
            ruleminingResult_User.append([interpretation,'Privacy Layer',x_Support,x_Support1, x_confidence ,sortedAntecedents,'T1',str(x_user),'PSMP'])


#User Behavior level2

for index, row in filtered_result_2.iterrows():
    x_antecedents=filtered_result_2.loc[index].at["antecedents"]
    x_consequents=filtered_result_2.loc[index].at["consequents"]
    x_antecedentSupport=int(filtered_result_2.loc[index].at["antecedent support"]*(number_of_deviations))
    x_consequentSupport=int(filtered_result_2.loc[index].at["consequent support"]*(number_of_deviations))
    x_Support=int(filtered_result_2.loc[index].at["support"]*(number_of_deviations))
    x_Support1=filtered_result_2.loc[index].at["support"]
    x_confidence=filtered_result_2.loc[index].at["confidence"]
    x_activity =''
    x_operation=''
    x_role=''
    x_erole=''
    x_user=''
    x_interpretation=''
    sortedAntecedents='' 
    sortedAntecedents2=''
    
    fs1 = x_antecedents
    for x in fs1:
        behavior=str(x) 
        if(behavior.startswith('a:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('ca:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('PLa:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('d:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('cd:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('DLd:')):
             x_operation=x.split(':')[1]
        if(behavior.startswith('r:')):
            x_role=x.split(':')[1]
        if(behavior.startswith('r_e:')):
            x_erole=x.split(':')[1]
        if(behavior.startswith('u:')):
            x_user=x.split(':')[1] 

    if(x_erole.startswith('*NN')):
        x_erole=''
    if(x_operation.startswith('*NA')):
        x_operation=''
    if(x_activity.startswith('*NA')):
        x_activity=''
    if(x_role.startswith('*NA')):
        x_role='' 
    sortedAntecedents=x_operation+'_'+x_activity+'_'+x_role+'_'+x_user
    sortedAntecedents2=x_operation+'_'+x_activity+'_'+x_erole+'_'+x_user
    
    fs2 = x_consequents
    for y in fs2:
        deviation=str(y) 
        if(deviation.startswith('dev:TSMP') & (x_operation !='') & (x_user !='')):
            interpretation='    +'+str(x_Support)+' times '+ ' illegal data operation  '+x_operation+'.'
            ruleminingResult_User.append([interpretation,'Data Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,sortedAntecedents2,'T2',str(x_user),'TSMP'])      
        if(deviation.startswith('dev:DL') & (x_operation !='') & (x_user !='')):
            interpretation='    -'+str(x_Support)+' times '+ ' unexpected data operation '+x_operation+'.'
            ruleminingResult_User.append([interpretation ,'Data Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,'NaE_'+sortedAntecedents2+'Z','T2',str(x_user),'DL'])
        if(deviation.startswith('dev:PSMP') & (x_activity !='') & (x_user !='')):
            interpretation='    +'+str(x_Support)+' times '+ ' illegal activity  '+x_activity+'.'
            ruleminingResult_User.append([interpretation ,'Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,sortedAntecedents2+'Z','T2',str(x_user),'PSMP'])     
        if(deviation.startswith('dev:PL') & (x_activity !='') & (x_user !='')):
            interpretation='    -'+str(x_Support)+' times '+ ' unexpected activity '+x_activity+'.'
            ruleminingResult_User.append([interpretation ,'Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,sortedAntecedents2+'Z','T2',str(x_user),'PL']) 

#User Behavior level3

for index, row in filtered_result_3.iterrows():
    x_antecedents=filtered_result_3.loc[index].at["antecedents"]
    x_consequents=filtered_result_3.loc[index].at["consequents"]
    x_antecedentSupport=int(filtered_result_3.loc[index].at["antecedent support"]*(number_of_deviations))
    x_consequentSupport=int(filtered_result_3.loc[index].at["consequent support"]*(number_of_deviations))
    x_Support=int(filtered_result_3.loc[index].at["support"]*(number_of_deviations))
    x_Support1=filtered_result_3.loc[index].at["support"]
    x_confidence=filtered_result_3.loc[index].at["confidence"]
    x_activity =''
    x_operation=''
    x_role=''
    x_erole=''
    x_user=''
    x_interpretation=''
    sortedAntecedents='' 
    sortedAntecedents2=''
    
    fs1 = x_antecedents
    for x in fs1:
        behavior=str(x) 
        if(behavior.startswith('a:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('ca:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('PLa:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('d:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('cd:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('DLd:')):
             x_operation=x.split(':')[1]
        if(behavior.startswith('r:')):
            x_role=x.split(':')[1]
        if(behavior.startswith('r_e:')):
            x_erole=x.split(':')[1]
        if(behavior.startswith('u:')):
            x_user=x.split(':')[1] 

    if(x_erole.startswith('*NN')):
        x_erole=''
    if(x_operation.startswith('*NA')):
        x_operation=''
    if(x_activity.startswith('*NA')):
        x_activity=''
    if(x_role.startswith('*NA')):
        x_role='' 
    sortedAntecedents=x_operation+'_'+x_activity+'_'+x_role+'_'+x_user
    sortedAntecedents2=x_operation+'_'+x_activity+'_'+x_erole+'_'+x_user
    
    fs2 = x_consequents
    for y in fs2:
        deviation=str(y) 
        if(deviation.startswith('dev:PSMP') & (x_user !='') & (x_activity !='') & (x_erole !='')):
            interpretation='         -'+str(x_Support)+' times '+ ' with unexpected role '+x_erole+'.'
            ruleminingResult_User.append([interpretation,'Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence , sortedAntecedents2,'T3',str(x_user),'PSMP'])
        if(deviation.startswith('dev:TSMP') & (x_activity !='') & (x_operation !='') & (x_user !='')):
            interpretation='         +'+str(x_Support)+' times '+ ' in the context of activity '+x_activity+'.'
            ruleminingResult_User.append([interpretation,'DataLayer & Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence , sortedAntecedents2,'T3',str(x_user),'TSMP'])

#User Behavior level4
for index, row in filtered_result_4.iterrows():
    x_antecedents=filtered_result_4.loc[index].at["antecedents"]
    x_consequents=filtered_result_4.loc[index].at["consequents"]
    x_antecedentSupport=int(filtered_result_4.loc[index].at["antecedent support"]*(number_of_deviations))
    x_consequentSupport=int(filtered_result_4.loc[index].at["consequent support"]*(number_of_deviations))
    x_Support=int(filtered_result_4.loc[index].at["support"]*(number_of_deviations))
    x_Support1=filtered_result_4.loc[index].at["support"]
    x_confidence=filtered_result_4.loc[index].at["confidence"]
    x_activity =''
    x_operation=''
    x_role=''
    x_erole=''
    x_user=''
    x_interpretation=''
    sortedAntecedents='' 
    sortedAntecedents2=''
    
    fs1 = x_antecedents
    for x in fs1:
        behavior=str(x) 
        if(behavior.startswith('a:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('ca:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('PLa:')):
            x_activity=x.split(':')[1]
        if(behavior.startswith('d:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('cd:')):
            x_operation=x.split(':')[1]
        if(behavior.startswith('DLd:')):
             x_operation=x.split(':')[1]
        if(behavior.startswith('r:')):
            x_role=x.split(':')[1]
        if(behavior.startswith('r_e:')):
            x_erole=x.split(':')[1]
        if(behavior.startswith('u:')):
            x_user=x.split(':')[1] 

    if(x_erole.startswith('*NN')):
        x_erole=''
    if(x_operation.startswith('*NA')):
        x_operation=''
    if(x_activity.startswith('*NA')):
        x_activity=''
    if(x_role.startswith('*NA')):
        x_role='' 
    sortedAntecedents=x_operation+'_'+x_activity+'_'+x_role+'_'+x_user
    sortedAntecedents2=x_operation+'_'+x_activity+'_'+x_erole+'_'+x_user
    
    fs2 = x_consequents
    for y in fs2:
        deviation=str(y) 
        if(deviation.startswith('dev:TSMP') & (x_role!='') ):
            interpretation='              -'+str(x_Support)+' times '+ ' with unexpected role '+x_role+'.'
            ruleminingResult.append([interpretation ,'Data Layer & Process Layer & Privacy Layer', x_Support,x_Support1, x_confidence ,sortedAntecedents2,'T4',str(x_user),'TSMP'])

filtered_result5=rm_results[ (rm_results['alength'] == 2) &
                            ( ( rm_results['antecedents'].astype(str).str.contains('u:')) & ( rm_results['antecedents'].astype(str).str.find('u:missing')== -1)) &
                            (( rm_results['antecedents'].astype(str).str.contains('r_e:'))) &
                            (rm_results['clength'] == 1) & (rm_results['consequents'].astype(str).str.contains('dev:')) &
                            (rm_results['confidence'] == 1)]


for index1, row1 in filtered_result5.iterrows():
    x_antecedents=filtered_result5.loc[index1].at["antecedents"]
    x_consequents=filtered_result5.loc[index1].at["consequents"]
    x_antecedentSupport=int(filtered_result5.loc[index1].at["antecedent support"]*(number_of_deviations))
    x_consequentSupport=int(filtered_result5.loc[index1].at["consequent support"]*(number_of_deviations))
    x_Support=int(filtered_result5.loc[index1].at["support"]*(number_of_deviations))
    x_Support1=filtered_result5.loc[index1].at["support"]
    x_confidence=filtered_result5.loc[index1].at["confidence"]
    x_activity = ''
    x_operation= ''
    x_role= ''
    x_erole= ''
    x_user=''
    interpretation=''
    sortedAntecedents='' 
    sortedAntecedents2=''
    
    fs3 = x_antecedents
    for k in fs3:
        behavior=str(k)
        if(behavior.startswith('u:')):
            user=k.split(':')[1]
        if(behavior.startswith('r_e:')):
            user_role=k.split(':')[1]
    if(x_erole.startswith('*NN')):
        x_erole='Unknown'
    if(x_operation.startswith('*NA')):
        x_operation=''
    if(x_activity.startswith('*NA')):
        x_activity=''
    if(x_role.startswith('*NA')):
        x_role='' 
    sortedAntecedents=x_operation+'_'+x_activity+'_'+x_role+'_'+x_user
    
    fs4 = x_consequents
    for l in fs4:
        deviation=str(l)     
        if(deviation.startswith('dev:DL')):
            interpretation='The user ('+str(user) +') with role ('+str(user_role)+') had only one kind of deviating behavior. This user performed unexpected data operations.'
            ruleminingResult_User.append([interpretation,'User', x_antecedentSupport,x_Support1, x_confidence ,sortedAntecedents,'T0',str(user),'DL'])
        if(deviation.startswith('dev:PL')):
            interpretation='The user: ('+str(user) +') with role ('+str(user_role)+') had only one kind of deviating behavior. This user performed unexpected activities.'
            ruleminingResult_User.append([interpretation,'User',x_antecedentSupport,x_Support1, x_confidence, sortedAntecedents,'T0',str(user),'PL'])
        if(deviation.startswith('dev:TSMP')):
            interpretation='The user: ('+str(user) +') with role ('+str(user_role)+') had only one kind of deviating behavior.'
            ruleminingResult_User.append([interpretation, x_antecedentSupport,x_Support1, x_confidence,sortedAntecedents,'T0',str(user),'TSMP'])
        if(deviation.startswith('dev:PSMP')):
            interpretation='The user: ('+str(user) +') with role ('+str(user_role)+') had only one kind of deviating behavior. This user performed activities illegally.'
            ruleminingResult_User.append([interpretation,'User', x_antecedentSupport,x_Support1, x_confidence ,sortedAntecedents,'T0',str(user),'PSMP']) 
            
#****************Select threshold*************************************
confidence_threshold=0.25
filtered_result6=rm_results[ (rm_results['alength'] == 1) &
                            (( rm_results['antecedents'].astype(str).str.contains('u:')) & ( rm_results['antecedents'].astype(str).str.find('u:missing')== -1)) &
                            (rm_results['clength'] == 1) & (rm_results['consequents'].astype(str).str.contains('dev:')) &
                            (rm_results['confidence'] <= confidence_threshold)]

mdeviating_users= set()
for index1, row1 in filtered_result6.iterrows():
    
    x1_antecedents=filtered_result6.loc[index1].at["antecedents"]
    x1_consequents=filtered_result6.loc[index1].at["consequents"]
    
    fs5 = x1_antecedents
    for k in fs5:
        behavior=str(k)
        if(behavior.startswith('u:')):
            user=k.split(':')[1]
            mdeviating_users.add(user)
            
ruleminingResult_User.append(['User(s) {'+str(mdeviating_users)+'} had the highest number of deviating behaviors and were involved in different types of deviations.','User','-','-','-','z','T0'])

#Step7: Categorizing the results (Users Deviating Patterns)
ruleminingResult_User
df_interpretations= pd.DataFrame(ruleminingResult_User,columns=['Deviating Behaviors', 'Perspective(s)','occurrence','Support','confidence','behavior','TreeStructure','User','Dev'])
print(df_interpretations)
df_interpretations.sort_values(by=['User','Dev','TreeStructure'], inplace=True, ascending = [False,False,True])
df_interpretations.reset_index(drop=True, inplace=True)

df_UserDeviatingPattern=df_interpretations[['Deviating Behaviors','occurrence','Support','confidence']] 
df_UserDeviatingPattern.to_csv('UserDeviatingPatterns.csv', index=True, sep=';')

