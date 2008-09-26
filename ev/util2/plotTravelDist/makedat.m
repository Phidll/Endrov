dat=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/traveldist.txt');
tstart=dat.data(:,1);
tend=dat.data(:,2);
dstraight=dat.data(:,3);
dfractal=dat.data(:,4);
rav=dat.data(:,5);
lifedev=dat.data(:,6);


%dur=(tend-tstart);   %todo frametime?


bins=linspace(1,12,10);
%[N,X]=hist(dstraight);
[N,X]=hist(dstraight,bins);

%write dat-file
%out=[N,X];
fp=fopen('traveldisthist.dat','wt');
for i=1:length(N)
    fprintf(fp,'%f\t%f\n',N(i),X(i));
end
fclose(fp);


lifedev=lifedev(lifedev~=0);
%hist(lifedev,15)

bins=linspace(0,0.4,15);
%[N,X]=hist(dstraight);
[N,X]=hist(lifedev,bins);
fp=fopen('divdevhist.dat','wt');
for i=1:length(N)
    fprintf(fp,'%f\t%f\n',N(i),X(i));
end
fclose(fp);
